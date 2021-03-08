INSERT INTO `user` (`id`, `username`, `password`) values (0, 'admin', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
                                                         (1, 'artist', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');
INSERT INTO `artist` (`id`, `name`) values (0, 'Unknown Artist'),
                                           (1, 'K-391');
INSERT INTO `song` (`id`, `title`, `artist_id`,`path`) values (0, 'test_song_mp3', 0, 'songs/1.mp3'),
                                                              (1, 'test_song_wav', 0, 'songs/1.wav'),
                                                              (2, 'Back In Time', 1, 'songs/K-391 - Back In Time.mp3'),
                                                              (3, 'Buoyancy', 1, 'songs/K-391 - Buoyancy.mp3'),
                                                              (4, 'Earth', 1, 'songs/K-391 - Earth.mp3'),
                                                              (5, 'Universe', 1, 'songs/K-391 - Universe.mp3');
INSERT INTO `playlist` (`id`, `author`, `name`) VALUES (0, '', 'no playlist'),
                                                       (1, 'test_author', 'test_name'),
                                                       (2, 'Crow08', 'Back in Time');
INSERT INTO `song_playlist` (`playlist_id`, `song_id`) VALUES ('1', '1'), ('1', '0');
INSERT INTO `song_playlist` (`playlist_id`, `song_id`) VALUES ('2', '2'), ('2', '3'), ('2', '4'), ('2', '5');

INSERT INTO `genre` (`id`, `name`) VALUES (1, 'Electronic'),
                                          (2, 'Rock');

update hibernate_sequence set next_val= 6
