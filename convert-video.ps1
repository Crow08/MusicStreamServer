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

function Get-Streams([string] $file) {
  $json = & ffprobe -v error -show_streams -of json -- $file | Out-String
  if ($LASTEXITCODE -ne 0) { throw "ffprobe failed for '$file'" }
  $streams = (ConvertFrom-Json $json).streams
  foreach ($type in 'video', 'audio', 'subtitle') {
    $n = 0
    foreach ($s in @($streams | Where-Object { $_.codec_type -eq $type })) {
      $s | Add-Member -NotePropertyName typeIndex -NotePropertyValue $n
      $n++
    }
  }
  return $streams
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

$inputs = @()
foreach ($p in $Path) {
  # Literal first: names like "[Group]Show[1080p].mkv" are wildcard patterns for -Path
  $resolved = @(Resolve-Path -LiteralPath $p -ErrorAction SilentlyContinue)
  if ($resolved.Count -eq 0) { $resolved = @(Resolve-Path -Path $p -ErrorAction SilentlyContinue) }
  if ($resolved.Count -eq 0) { throw "Input not found: $p" }
  $inputs += $resolved | ForEach-Object { $_.ProviderPath }
}
if ($Output -and $inputs.Count -gt 1) { throw '-Output can only be used with a single input file, use -OutputDir instead.' }

$failed = @()
foreach ($in in $inputs) {
  # An error in one file must not abort a batch run: skip the file and report it at the end
  try {
    $streams = Get-Streams $in
    $video = @($streams | Where-Object { $_.codec_type -eq 'video' -and $_.disposition.attached_pic -ne 1 })
    $audio = @($streams | Where-Object { $_.codec_type -eq 'audio' })
    $subs = @($streams | Where-Object { $_.codec_type -eq 'subtitle' })

    Write-Host "`n== $in"
    if ($ListStreams) {
      foreach ($s in $video + $audio + $subs) { Write-Host "  $($s.codec_type): $(Format-Stream $s)" }
      continue
    }
    if ($video.Count -eq 0) { throw 'No video stream.' }

    $selectedAudio = Select-Stream $audio 'audio' $AudioTrack $AudioLanguage $false
    $selectedSub = $null
    if (-not $NoSubtitles) {
      $selectedSub = Select-Stream $subs 'subtitle' $SubtitleTrack $SubtitleLanguage $true
    }

    if ($Output) {
      $out = $Output
    } else {
      $dir = if ($OutputDir) { $OutputDir } else { Split-Path $in -Parent }
      $out = Join-Path $dir ([IO.Path]::GetFileNameWithoutExtension($in) + '.mp4')
    }
    $out = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($out)
    if ($out -eq $in) { throw "Output would overwrite the input: $out" }
    if ((Test-Path -LiteralPath $out) -and -not $Force) {
      throw "Output exists (use -Force to overwrite): $out"
    }
    $outDir = Split-Path $out -Parent
    if (-not (Test-Path -LiteralPath $outDir)) { New-Item -ItemType Directory -Path $outDir | Out-Null }

    $v = $video[0]
    $ffArgs = @('-hide_banner', '-loglevel', 'error', '-stats', '-y', '-i', $in)
    if ($Start -gt 0) {
      # Output seeking keeps the original timestamps for the subtitle renderer
      $ffArgs += '-ss', $Start
    }
    if ($Duration -gt 0) { $ffArgs += '-t', $Duration }

    if ($selectedSub -and $bitmapSubtitleCodecs -contains $selectedSub.codec_name) {
      $graph = "[0:$($v.index)][0:$($selectedSub.index)]overlay=eof_action=pass,format=yuv420p[v]"
      $ffArgs += '-filter_complex', $graph, '-map', '[v]'
    } elseif ($selectedSub) {
      # si = index among the subtitle streams; libass also loads the fonts attached to the file
      $filter = "format=yuv420p,subtitles=filename=$(ConvertTo-FilterValue $in):si=$($selectedSub.typeIndex)"
      $ffArgs += '-filter_complex', "[0:$($v.index)]$filter[v]", '-map', '[v]'
    } else {
      $ffArgs += '-map', "0:$($v.index)", '-vf', 'format=yuv420p'
    }

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

    Write-Host "  video:    $(Format-Stream $v) -> h264 8-bit, crf $Crf, preset $Preset$(if ($Tune) { ", tune $Tune" })"
    if ($selectedAudio) { Write-Host "  audio:    $(Format-Stream $selectedAudio)" } else { Write-Host '  audio:    none' }
    if ($selectedSub) { Write-Host "  subtitle: $(Format-Stream $selectedSub) (burned in)" } else { Write-Host '  subtitle: none' }
    Write-Host "  output:   $out"

    # ffmpeg writes progress to stderr; Windows PowerShell must not treat that as an error
    $ErrorActionPreference = 'Continue'
    & ffmpeg @ffArgs
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($exitCode -ne 0) {
      Remove-Item -LiteralPath $out -ErrorAction SilentlyContinue
      throw "ffmpeg failed (exit code $exitCode)."
    }
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
