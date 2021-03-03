INSERT INTO `user`(`id`, `username`, `password`) values (0, 'admin', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');
INSERT INTO `user`(`id`, `username`, `password`) values (1, 'user', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');
INSERT INTO `song`(`id`, `title`, `artist`,`path`) values (0, 'test_song_mp3', null, 'songs/1.mp3');
INSERT INTO `song`(`id`, `title`, `artist`,`path`) values (1, 'test_song_wav', 'test_artist', 'songs/1.wav');
INSERT INTO `playlist` (`id`, `author`, `name`) VALUES (0, 'test_author', 'test_name');
INSERT INTO `song_playlist` (`playlist_id`, `song_id`) VALUES ('0', '1'), ('0', '0');
