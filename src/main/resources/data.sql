INSERT INTO `user` (`id`, `username`, `password`)
values (1, 'admin', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
       (2, 'user', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
       (3, 'Crow08', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');

INSERT INTO `artist` (`id`, `name`)
values (1, 'Unknown Artist'),
       (2, 'K-391'),
       (3, 'Bleach');

INSERT INTO `album` (`id`, `name`)
values (1, 'Unknown Album'),
       (2, 'Back In Time'),
       (3, 'Thousand-Year Blood War');

INSERT INTO `genre` (`id`, `name`)
VALUES (1, 'Electronic'),
       (2, 'Rock');

INSERT INTO `tag` (`id`, `name`)
VALUES (1, 'spotify'),
       (2, 'Bad Quality'),
       (3, 'Wrong Version');

INSERT INTO `media` (`id`, `title`, `uri`, `artist_id`, `album_id`)
values (1, 'test_song_mp3', '/media/1.mp3', 1, 1),
       (2, 'test_song_wav', '/media/1.wav', 1, 1),
       (3, 'Back In Time', '/media/K-391 - Back In Time.mp3', 2, 2),
       (4, 'Buoyancy', '/media/K-391 - Buoyancy.mp3', 2, 2),
       (5, 'Earth', '/media/K-391 - Earth.mp3', 2, 2),
       (6, 'Universe', '/media/K-391 - Universe.mp3', 2, 2),
       (7, 'video_Folge_1', '/media/v1.mp4', 3, 3),
       (8, 'video_Folge_2', '/media/v2.mp4', 3, 3),
       (9, 'video_Folge_3', '/media/v3.mp4', 3, 3),
       (10, 'video_Folge_4', '/media/v4.mp4', 3, 3),
       (11, 'video_Folge_5', '/media/v5.mkv', 3, 3);

INSERT INTO `playlist` (`id`, `author_id`, `name`)
VALUES (1, null, 'no playlist'),
       (2, 1, 'test_name'),
       (3, 2, 'Back in Time'),
       (4, 3, 'Bleach');

INSERT INTO `song_playlist` (`playlist_id`, `song_id`)
VALUES (2, 1),
       (2, 2),
       (3, 3),
       (3, 4),
       (3, 5),
       (3, 6),
       (4, 7),
       (4, 8),
       (4, 9),
       (4, 10),
       (4, 11);

INSERT INTO `song_genre` (`genre_id`, `song_id`)
VALUES (1, 3),
       (1, 4),
       (2, 5),
       (2, 6);
