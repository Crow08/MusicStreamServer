INSERT INTO `user` (`id`, `username`, `password`)
values (1, 'admin', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
       (2, 'user', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
       (3, 'Crow08', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');

INSERT INTO `artist` (`id`, `name`)
values (1, 'Unknown Artist'),
       (2, 'K-391');

INSERT INTO `series` (`id`, `name`)
values (1, 'Unknown Series'),
       (2, 'Bleach');

INSERT INTO `album` (`id`, `name`)
values (1, 'Unknown Album'),
       (2, 'Back In Time');

INSERT INTO `season` (`id`, `name`)
values (1, 'Unknown Season'),
       (2, 'Thousand-Year Blood War');

INSERT INTO `genre` (`id`, `name`)
VALUES (1, 'Electronic'),
       (2, 'Rock');

INSERT INTO `tag` (`id`, `name`)
VALUES (1, 'spotify'),
       (2, 'Bad Quality'),
       (3, 'Wrong Version');

INSERT INTO `media` (`id`, `title`, `uri`, `type`)
values (1, 'test_song_mp3', '/songs/1.mp3', 'SONG'),
       (2, 'test_song_wav', '/songs/1.wav', 'SONG'),
       (3, 'Back In Time', '/songs/K-391 - Back In Time.mp3', 'SONG'),
       (4, 'Buoyancy', '/songs/K-391 - Buoyancy.mp3', 'SONG'),
       (5, 'Earth', '/songs/K-391 - Earth.mp3', 'SONG'),
       (6, 'Universe', '/songs/K-391 - Universe.mp3', 'SONG'),
       (7, '01 Shinigami', '/videos/v1.mp4', 'VIDEO'),
       (8, '02 Foundation Stones', '/videos/v2.mp4', 'VIDEO'),
       (9, '03 March of the StarCross', '/videos/v3.mp4', 'VIDEO'),
       (10, '04 Kill the Shadow', '/videos/v4.mp4', 'VIDEO'),
       (11, '05 Wrath as a Lightning', '/videos/v5.mp4', 'VIDEO'),
       (12, '06 The Fire', '/videos/v6.mp4', 'VIDEO'),
       (13, '07 Born in the Dark', '/videos/v7.mp4', 'VIDEO'),
       (14, '08 The Shooting Star Project', '/videos/v8.mp4', 'VIDEO'),
       (15, '09 The Drop', '/videos/v9.mp4', 'VIDEO'),
       (16, '10 The Battle', '/videos/v10.mp4', 'VIDEO'),
       (17, '11 Everything but the Rain', '/videos/v11.mp4', 'VIDEO'),
       (18, '12-13 June Truth / The Blade is me', '/videos/v12-13.mp4', 'VIDEO');

INSERT INTO `song` (`id`, `artist_id`, `album_id`, `spotify`)
values (1, 1, 1, FALSE),
       (2, 1, 1, FALSE),
       (3, 2, 2, FALSE),
       (4, 2, 2, FALSE),
       (5, 2, 2, FALSE),
       (6, 2, 2, FALSE);

INSERT INTO `video` (`id`, `series_id`, `season_id`)
values (7, 2, 2),
       (8, 2, 2),
       (9, 2, 2),
       (10, 2, 2),
       (11, 2, 2),
       (12, 2, 2),
       (13, 2, 2),
       (14, 2, 2),
       (15, 2, 2),
       (16, 2, 2),
       (17, 2, 2),
       (18, 2, 2);

INSERT INTO `playlist` (`id`, `author_id`, `name`)
VALUES (1, null, 'no playlist'),
       (2, 1, 'test_name'),
       (3, 2, 'Back in Time'),
       (4, 3, 'Bleach');

INSERT INTO `media_playlist` (`playlist_id`, `media_id`)
VALUES (2, 1),
       (2, 2),
       (3, 3),
       (3, 4),
       (3, 5),
       (3, 6),
       (4, 16),
       (4, 17),
       (4, 18);

INSERT INTO `song_genre` (`genre_id`, `song_id`)
VALUES (1, 3),
       (1, 4),
       (2, 5),
       (2, 6);
