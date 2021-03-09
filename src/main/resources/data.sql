INSERT INTO `user` (`id`, `username`, `password`) values (0, 'admin', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS'),
                                                         (1, 'user', '{bcrypt}$2a$10$rLF5EuZIU6GK.EaT9R.JDO0Hg/UDS1aorA8WRKxpeA7xF3AaXAKcS');

INSERT INTO `artist` (`id`, `name`) values (0, 'Unknown Artist'),
                                           (1, 'K-391');

INSERT INTO `genre` (`id`, `name`) VALUES (1, 'Electronic'),
                                          (2, 'Rock');

INSERT INTO `song` (`id`, `title`, `path`, `artist_id`) values (0, 'test_song_mp3', 'songs/1.mp3',0 ),
                                                               (1, 'test_song_wav', 'songs/1.wav', 0),
                                                               (2, 'Back In Time', 'songs/K-391 - Back In Time.mp3', 1),
                                                               (3, 'Buoyancy', 'songs/K-391 - Buoyancy.mp3', 1),
                                                               (4, 'Earth', 'songs/K-391 - Earth.mp3', 1),
                                                               (5, 'Universe', 'songs/K-391 - Universe.mp3', 1);

INSERT INTO `playlist` (`id`, `author_id`, `name`) VALUES (0, null, 'no playlist'),
                                                          (1, 1, 'test_name'),
                                                          (2, 0, 'Back in Time');

INSERT INTO `song_playlist` (`playlist_id`, `song_id`) VALUES (1, 1), (1, 0),
                                                              (2, 2), (2, 3), (2, 4), (2, 5);

INSERT INTO `song_genre` (`genre_id`, `song_id`) VALUES (1, 2), (1, 3), (1, 4), (1, 5);

update hibernate_sequence set next_val= 6
