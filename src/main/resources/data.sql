INSERT INTO `user`(`id`, `username`, `password`) values (0, 'admin', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
                                                        (1, 'user', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');
INSERT INTO `song`(`id`, `title`, `artist`,`path`) values (0, 'test_song_mp3', null, 'songs/1.mp3'),
                                                          (1, 'test_song_wav', 'test_artist', 'songs/1.wav'),
                                                          (2, 'Back In Time', 'K-391', 'songs/K-391 - Back In Time.mp3'),
                                                          (3, 'Buoyancy', 'K-391', 'songs/K-391 - Buoyancy.mp3'),
                                                          (4, 'Earth', 'K-391', 'songs/K-391 - Earth.mp3'),
                                                          (5, 'Universe', 'K-391', 'songs/K-391 - Universe.mp3');
INSERT INTO `playlist` (`id`, `author`, `name`) VALUES (0, 'test_author', 'test_name'),
                                                       (1, 'Crow08', 'Back in Time');
INSERT INTO `song_playlist` (`playlist_id`, `song_id`) VALUES ('0', '1'), ('0', '0');
INSERT INTO `song_playlist` (`playlist_id`, `song_id`) VALUES ('1', '2'), ('1', '3'), ('1', '4'), ('1', '5');
