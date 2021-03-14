INSERT INTO `user` (`id`, `username`, `password`) values (1, 'admin', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
                                                         (2, 'user', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');

INSERT INTO `artist` (`id`, `name`) values (1, 'Unknown Artist'),
                                           (2, 'K-391');

INSERT INTO `album` (`id`, `name`) values (1, 'Unknown Album'),
                                          (2, 'Back In Time');

INSERT INTO `genre` (`id`, `name`) VALUES (1, 'Electronic'),
                                          (2, 'Rock');

INSERT INTO `tag` (`id`, `name`) VALUES (1, 'Bad Quality'),
                                          (2, 'Wrong Version');

INSERT INTO `song` (`id`, `title`, `path`, `artist_id`, `album_id`) values (1, 'test_song_mp3', '/songs/1.mp3',1, 1 ),
                                                               (2, 'test_song_wav', '/songs/1.wav', 1, 1),
                                                               (3, 'Back In Time', '/songs/K-391 - Back In Time.mp3', 2, 2),
                                                               (4, 'Buoyancy', '/songs/K-391 - Buoyancy.mp3', 2, 2),
                                                               (5, 'Earth', '/songs/K-391 - Earth.mp3', 2, 2),
                                                               (6, 'Universe', '/songs/K-391 - Universe.mp3', 2, 2);

INSERT INTO `playlist` (`id`, `author_id`, `name`) VALUES (1, null, 'no playlist'),
                                                          (2, 1, 'test_name'),
                                                          (3, 2, 'Back in Time');

INSERT INTO `song_playlist` (`playlist_id`, `song_id`) VALUES (2, 1), (2, 2),
                                                              (3, 3), (3, 4), (3, 5), (3, 6);

INSERT INTO `song_genre` (`genre_id`, `song_id`) VALUES (1, 3), (1, 4), (1, 5), (1, 6);
