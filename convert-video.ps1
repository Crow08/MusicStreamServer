<#
.SYNOPSIS
  Converts videos (e.g. MKV with ASS subtitles) into browser-playable MP4 files with burned-in subtitles.

.DESCRIPTION
  Output: H.264 High profile, 8-bit 4:2:0 (10-bit H.264 cannot be decoded by browsers), AAC audio,
  moov atom at the front (fast start and seeking), chapters kept. Only one video, one audio and at most
  one burned-in subtitle track end up in the output.

  Text subtitles (ASS/SSA/SRT/...) are rendered with libass including the fonts attached to the file,
  bitmap subtitles (PGS/VobSub/DVB) are overlaid. If a file has several subtitle or audio tracks and
  none is selected via parameters, the script asks which one to use.

  A thumbnail sprite for the seek bar preview is written next to the output (<name>.thumbs.jpg): one
  160x90 frame every 2 s, 10 per row, taken before the subtitles are burned in. -ThumbnailsOnly creates
  only the sprite for existing videos.

  Requires ffmpeg and ffprobe on the PATH (e.g. winget install Gyan.FFmpeg).

.EXAMPLE
  .\convert-video.ps1 -ListStreams episode.mkv

.EXAMPLE
  .\convert-video.ps1 episode.mkv -Output F:\media\videos\v27.mp4

.EXAMPLE
  .\convert-video.ps1 F:\new\*.mkv -OutputDir F:\media\videos -SubtitleLanguage ger -Tune animation

.EXAMPLE
  # Encode only one minute starting at 5:00 to check quality and subtitle timing
  .\convert-video.ps1 episode.mkv -Output preview.mp4 -Start 300 -Duration 60

.EXAMPLE
  # Thumbnail sprites for already converted videos
  .\convert-video.ps1 -ThumbnailsOnly F:\media\videos\v*.mp4
#>
[CmdletBinding()]
param(
  [Parameter(Mandatory = $true, Position = 0, ValueFromRemainingArguments = $true)]
  [string[]] $Path,
  # Output file (only for a single input). Default: <input name>.mp4 next to the input or in -OutputDir.
  [string] $Output,
  [string] $OutputDir,
  # Subtitle track number among the subtitle tracks (0 = first), as shown by -ListStreams.
  [int] $SubtitleTrack = -1,
  # ISO 639-2 language code of the subtitle track, e.g. ger, eng.
  [string] $SubtitleLanguage,
  [switch] $NoSubtitles,
  # Audio track number among the audio tracks (0 = first), as shown by -ListStreams.
  [int] $AudioTrack = -1,
  [string] $AudioLanguage,
  # x264 quality: lower is better and larger. 18 is visually close to lossless, 20-22 is smaller.
  [ValidateRange(0, 51)] [int] $Crf = 18,
  # x264 speed/efficiency trade-off: slower = smaller file at the same quality.
  [ValidateSet('ultrafast', 'superfast', 'veryfast', 'faster', 'fast', 'medium', 'slow', 'slower', 'veryslow')]
  [string] $Preset = 'slow',
  # x264 tuning, e.g. 'animation' for anime, 'film' for live action.
  [ValidateSet('', 'film', 'animation', 'grain', 'stillimage')]
  [string] $Tune = '',
  # Bitrate for audio that has to be re-encoded (AAC audio is copied unchanged).
  [string] $AudioBitrate = '256k',
  # Start offset and duration in seconds, to encode a short preview.
  [double] $Start = 0,
  [double] $Duration = 0,
  [switch] $ListStreams,
  # Do not create the thumbnail sprite.
  [switch] $NoThumbnails,
  # Only create the thumbnail sprite (<input name>.thumbs.jpg) for an existing video, no conversion.
  [switch] $ThumbnailsOnly,
  [switch] $Force
)

$ErrorActionPreference = 'Stop'

foreach ($tool in 'ffmpeg', 'ffprobe') {
  if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
    throw "$tool not found on the PATH. Install it e.g. with 'winget install Gyan.FFmpeg' and open a new shell."
  }
}

$bitmapSubtitleCodecs = 'hdmv_pgs_subtitle', 'dvd_subtitle', 'dvb_subtitle', 'xsub'
$browserAudioCodecs = 'aac'

function Get-Probe([string] $file) {
  $json = & ffprobe -v error -show_streams -show_format -of json -- $file | Out-String
  if ($LASTEXITCODE -ne 0) { throw "ffprobe failed for '$file'" }
  $probe = ConvertFrom-Json $json
  $streams = $probe.streams
  foreach ($type in 'video', 'audio', 'subtitle') {
    $n = 0
    foreach ($s in @($streams | Where-Object { $_.codec_type -eq $type })) {
      $s | Add-Member -NotePropertyName typeIndex -NotePropertyValue $n
      $n++
    }
  }
  return [pscustomobject]@{
    streams  = $streams
    duration = [double]::Parse($probe.format.duration, [Globalization.CultureInfo]::InvariantCulture)
  }
}

function Get-Tag($stream, [string] $name) {
  if ($stream.tags -and $stream.tags.PSObject.Properties[$name]) { return $stream.tags.$name }
  return ''
}

function Format-Stream($s) {
  $parts = @("#$($s.typeIndex)", $s.codec_name)
  switch ($s.codec_type) {
    'video' { $parts += "$($s.width)x$($s.height)", $s.pix_fmt, $s.profile }
    'audio' { $parts += "$($s.channels)ch" }
  }
  $lang = Get-Tag $s 'language'
  $title = Get-Tag $s 'title'
  if ($lang) { $parts += "[$lang]" }
  if ($title) { $parts += "'$title'" }
  if ($s.disposition.default -eq 1) { $parts += '(default)' }
  if ($s.disposition.forced -eq 1) { $parts += '(forced)' }
  return ($parts -join ' ')
}

function Select-Stream($streams, [string] $kind, [int] $track, [string] $language, [bool] $allowNone) {
  $streams = @($streams)
  if ($track -ge 0) {
    $match = $streams | Where-Object { $_.typeIndex -eq $track }
    if (-not $match) { throw "There is no $kind track #$track." }
    return $match
  }
  if ($language) {
    $match = @($streams | Where-Object { (Get-Tag $_ 'language') -eq $language })
    if ($match.Count -eq 0) {
      # Some releases leave single tracks untagged; use such a track if it is the only candidate
      $untagged = @($streams | Where-Object { @('', 'und') -contains (Get-Tag $_ 'language') })
      if ($untagged.Count -eq 1) {
        Write-Warning "No $kind track with language '$language', using the only untagged track: $(Format-Stream $untagged[0])"
        return $untagged[0]
      }
      throw "There is no $kind track with language '$language'."
    }
    # Prefer full subtitles over forced (signs only) tracks
    $full = @($match | Where-Object { $_.disposition.forced -ne 1 })
    if ($full.Count -gt 0) { return $full[0] }
    return $match[0]
  }
  if ($streams.Count -eq 0) { return $null }
  if ($streams.Count -eq 1) { return $streams[0] }

  Write-Host "Several $kind tracks found:"
  foreach ($s in $streams) { Write-Host "  $(Format-Stream $s)" }
  $prompt = "Which $kind track? (number"
  if ($allowNone) { $prompt += ", n = none" }
  $prompt += ')'
  while ($true) {
    $answer = (Read-Host $prompt).Trim()
    if ($allowNone -and $answer -eq 'n') { return $null }
    $number = 0
    if ([int]::TryParse($answer, [ref] $number)) {
      $match = $streams | Where-Object { $_.typeIndex -eq $number }
      if ($match) { return $match }
    }
    Write-Host 'Invalid input.'
  }
}

# Escapes a value for an ffmpeg filter option: quoted for the option parser, the result escaped again
# for the filtergraph parser (see "Notes on filtergraph escaping" in the ffmpeg-filters documentation).
function ConvertTo-FilterValue([string] $value) {
  $level1 = "'" + ($value -replace "'", "'\''") + "'"
  return ($level1 -replace '([\\\[\],;''])', '\$1')
}

# Thumbnail sprite for the seek bar preview. The client (player-track.component.ts) uses the same constants:
# tile n shows the time n * interval, at column n % columns and row floor(n / columns).
$thumbnailInterval = 2
$thumbnailWidth = 160
$thumbnailHeight = 90
$thumbnailColumns = 10
# Maximum JPEG height
$maxSpriteHeight = 65500

function Get-SpritePath([string] $mediaFile) {
  return Join-Path (Split-Path $mediaFile -Parent) ([IO.Path]::GetFileNameWithoutExtension($mediaFile) + '.thumbs.jpg')
}

# Filter chain that turns the video into a single sprite image, or $null if the video is too long for one image
function Get-ThumbnailFilter([double] $seconds) {
  $tiles = [math]::Floor($seconds / $thumbnailInterval) + 1
  $rows = [math]::Ceiling($tiles / $thumbnailColumns)
  if ($rows * $thumbnailHeight -gt $maxSpriteHeight) {
    Write-Warning "Video too long for a single thumbnail sprite ($rows rows), no thumbnails generated."
    return $null
  }
  $w = $thumbnailWidth
  $h = $thumbnailHeight
  return "fps=1/$thumbnailInterval,scale=${w}:${h}:force_original_aspect_ratio=decrease," +
    "pad=${w}:${h}:(ow-iw)/2:(oh-ih)/2,setsar=1,tile=${thumbnailColumns}x$rows"
}

function Get-SpriteOutputArgs([string] $sprite) {
  return @('-map', '[t]', '-frames:v', '1', '-q:v', '5', '-update', '1', $sprite)
}

# Runs ffmpeg; ffmpeg writes progress to stderr, which Windows PowerShell must not treat as an error
function Invoke-Ffmpeg([string[]] $ffArgs, [string[]] $outputs) {
  $ErrorActionPreference = 'Continue'
  & ffmpeg @ffArgs
  $exitCode = $LASTEXITCODE
  $ErrorActionPreference = 'Stop'
  if ($exitCode -ne 0) {
    foreach ($o in $outputs) { Remove-Item -LiteralPath $o -ErrorAction SilentlyContinue }
    throw "ffmpeg failed (exit code $exitCode)."
  }
}

function Resolve-OutputPath([string] $in, [string] $extension) {
  if ($Output) {
    $out = $Output
  } else {
    $dir = if ($OutputDir) { $OutputDir } else { Split-Path $in -Parent }
    $out = Join-Path $dir ([IO.Path]::GetFileNameWithoutExtension($in) + $extension)
  }
  $out = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($out)
  if ($out -eq $in) { throw "Output would overwrite the input: $out" }
  return $out
}

function Assert-Writable([string[]] $files) {
  foreach ($f in $files) {
    if ((Test-Path -LiteralPath $f) -and -not $Force) { throw "Output exists (use -Force to overwrite): $f" }
    $dir = Split-Path $f -Parent
    if (-not (Test-Path -LiteralPath $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
  }
}

$inputs = @()
foreach ($p in $Path) {
  # Literal first: names like "[Group]Show[1080p].mkv" are wildcard patterns for -Path
  $resolved = @(Resolve-Path -LiteralPath $p -ErrorAction SilentlyContinue)
  if ($resolved.Count -eq 0) { $resolved = @(Resolve-Path -Path $p -ErrorAction SilentlyContinue) }
  if ($resolved.Count -eq 0) { throw "Input not found: $p" }
  $inputs += $resolved | ForEach-Object { $_.ProviderPath }
}
if ($Output -and $inputs.Count -gt 1) { throw '-Output can only be used with a single input file, use -OutputDir instead.' }
if ($ThumbnailsOnly -and $NoThumbnails) { throw '-ThumbnailsOnly and -NoThumbnails cannot be combined.' }

$failed = @()
foreach ($in in $inputs) {
  # An error in one file must not abort a batch run: skip the file and report it at the end
  try {
    $probe = Get-Probe $in
    $streams = $probe.streams
    $video = @($streams | Where-Object { $_.codec_type -eq 'video' -and $_.disposition.attached_pic -ne 1 })
    $audio = @($streams | Where-Object { $_.codec_type -eq 'audio' })
    $subs = @($streams | Where-Object { $_.codec_type -eq 'subtitle' })

    Write-Host "`n== $in"
    if ($ListStreams) {
      foreach ($s in $video + $audio + $subs) { Write-Host "  $($s.codec_type): $(Format-Stream $s)" }
      continue
    }
    if ($video.Count -eq 0) { throw 'No video stream.' }
    $v = $video[0]

    # Length of the part that is converted
    $seconds = $probe.duration - $Start
    if ($Duration -gt 0 -and $Duration -lt $seconds) { $seconds = $Duration }
    if ($seconds -le 0) { throw "-Start is beyond the end of the video ($($probe.duration) s)." }

    if ($ThumbnailsOnly) {
      $sprite = Resolve-OutputPath $in '.thumbs.jpg'
      $thumbFilter = Get-ThumbnailFilter $seconds
      if (-not $thumbFilter) { throw 'No thumbnails generated.' }
      Assert-Writable @($sprite)
      Write-Host "  thumbnails: $sprite"
      # Input seeking is fine here: there are no subtitles that need the original timestamps
      $ffArgs = @('-hide_banner', '-loglevel', 'error', '-stats', '-y')
      if ($Start -gt 0) { $ffArgs += '-ss', $Start }
      if ($Duration -gt 0) { $ffArgs += '-t', $Duration }
      $ffArgs += '-i', $in, '-filter_complex', "[0:$($v.index)]$thumbFilter[t]"
      $ffArgs += Get-SpriteOutputArgs $sprite
      Invoke-Ffmpeg $ffArgs @($sprite)
      continue
    }

    $selectedAudio = Select-Stream $audio 'audio' $AudioTrack $AudioLanguage $false
    $selectedSub = $null
    if (-not $NoSubtitles) {
      $selectedSub = Select-Stream $subs 'subtitle' $SubtitleTrack $SubtitleLanguage $true
    }

    $out = Resolve-OutputPath $in '.mp4'
    $sprite = $null
    $thumbFilter = $null
    if (-not $NoThumbnails) {
      $thumbFilter = Get-ThumbnailFilter $seconds
      if ($thumbFilter) { $sprite = Get-SpritePath $out }
    }
    $outputs = @($out, $sprite) | Where-Object { $_ }
    Assert-Writable $outputs

    $ffArgs = @('-hide_banner', '-loglevel', 'error', '-stats', '-y', '-i', $in)
    if ($Start -gt 0) {
      # Output seeking keeps the original timestamps for the subtitle renderer
      $ffArgs += '-ss', $Start
    }
    if ($Duration -gt 0) { $ffArgs += '-t', $Duration }

    # The thumbnail branch is split off before the subtitles are burned in (unreadable at thumbnail size).
    # Output seeking does not apply to the filter graph, so that branch is trimmed to the same segment.
    $src = "[0:$($v.index)]"
    $thumbBranch = ''
    if ($thumbFilter) {
      $trim = ''
      if ($Start -gt 0 -or $Duration -gt 0) {
        $trim = "trim=start=$Start"
        if ($Duration -gt 0) { $trim += ":duration=$Duration" }
        $trim += ',setpts=PTS-STARTPTS,'
      }
      $thumbBranch = ";[th]$trim$thumbFilter[t]"
    }
    if ($selectedSub -and $bitmapSubtitleCodecs -contains $selectedSub.codec_name) {
      $overlay = "[0:$($selectedSub.index)]overlay=eof_action=pass,format=yuv420p[v]"
      $graph = if ($thumbFilter) { "${src}split[main][th];[main]$overlay" } else { "$src$overlay" }
    } elseif ($selectedSub) {
      # si = index among the subtitle streams; libass also loads the fonts attached to the file
      $subtitles = "subtitles=filename=$(ConvertTo-FilterValue $in):si=$($selectedSub.typeIndex)[v]"
      $graph = if ($thumbFilter) { "${src}format=yuv420p,split[main][th];[main]$subtitles" } else { "${src}format=yuv420p,$subtitles" }
    } else {
      $graph = if ($thumbFilter) { "${src}format=yuv420p,split[v][th]" } else { "${src}format=yuv420p[v]" }
    }
    $ffArgs += '-filter_complex', ($graph + $thumbBranch), '-map', '[v]'

    $ffArgs += '-c:v', 'libx264', '-preset', $Preset, '-crf', $Crf, '-profile:v', 'high'
    if ($Tune) { $ffArgs += '-tune', $Tune }

    if ($selectedAudio) {
      $ffArgs += '-map', "0:$($selectedAudio.index)"
      if ($browserAudioCodecs -contains $selectedAudio.codec_name) {
        $ffArgs += '-c:a', 'copy'
      } else {
        $ffArgs += '-c:a', 'aac', '-b:a', $AudioBitrate
      }
    }
    $ffArgs += '-map_chapters', '0', '-movflags', '+faststart', $out
    if ($sprite) { $ffArgs += Get-SpriteOutputArgs $sprite }

    Write-Host "  video:      $(Format-Stream $v) -> h264 8-bit, crf $Crf, preset $Preset$(if ($Tune) { ", tune $Tune" })"
    if ($selectedAudio) { Write-Host "  audio:      $(Format-Stream $selectedAudio)" } else { Write-Host '  audio:      none' }
    if ($selectedSub) { Write-Host "  subtitle:   $(Format-Stream $selectedSub) (burned in)" } else { Write-Host '  subtitle:   none' }
    Write-Host "  output:     $out"
    if ($sprite) { Write-Host "  thumbnails: $sprite" }

    Invoke-Ffmpeg $ffArgs $outputs
  } catch {
    Write-Warning "Skipped '$in': $($_.Exception.Message)"
    $failed += $in
  }
}

if ($failed.Count -gt 0) {
  Write-Host "`n$($failed.Count) of $($inputs.Count) file(s) failed:"
  foreach ($f in $failed) { Write-Host "  $f" }
  exit 1
}
