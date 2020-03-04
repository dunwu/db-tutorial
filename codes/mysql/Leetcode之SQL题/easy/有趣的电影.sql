--    【有趣的电影】
--
--    某城市开了一家新的电影院，吸引了很多人过来看电影。该电影院特别注意用户体验，专门有个 LED显示板做电影推荐，上面公布着影评和相关电影描述。
--
--    作为该电影院的信息部主管，您需要编写一个 SQL查询，找出所有影片描述为非 boring (不无聊) 的并且 id 为奇数 的影片，结果请按等级 rating 排列。
--
--
--
--    例如，下表 cinema:
--
--    +---------+-----------+--------------+-----------+
--    |   id    | movie     |  description |  rating   |
--    +---------+-----------+--------------+-----------+
--    |   1     | War       |   great 3D   |   8.9     |
--    |   2     | Science   |   fiction    |   8.5     |
--    |   3     | irish     |   boring     |   6.2     |
--    |   4     | Ice song  |   Fantacy    |   8.6     |
--    |   5     | House card|   Interesting|   9.1     |
--    +---------+-----------+--------------+-----------+
--    对于上面的例子，则正确的输出是为：
--
--    +---------+-----------+--------------+-----------+
--    |   id    | movie     |  description |  rating   |
--    +---------+-----------+--------------+-----------+
--    |   5     | House card|   Interesting|   9.1     |
--    |   1     | War       |   great 3D   |   8.9     |
--    +---------+-----------+--------------+-----------+

USE db_tutorial;
CREATE TABLE IF NOT EXISTS cinema (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    movie       VARCHAR(20),
    description VARCHAR(20),
    rating      DOUBLE
);

INSERT INTO cinema(movie, description, rating)
VALUES ('War', 'great 3D', 8.9);
INSERT INTO cinema(movie, description, rating)
VALUES ('Science', 'fiction', 8.5);
INSERT INTO cinema(movie, description, rating)
VALUES ('irish', 'boring', 6.2);
INSERT INTO cinema(movie, description, rating)
VALUES ('Ice song', 'Fantacy', 8.6);
INSERT INTO cinema(movie, description, rating)
VALUES ('House card', 'Interesting', 9.1);

-- 解题
SELECT *
FROM cinema
WHERE description != 'boring' AND id % 2 = 1
ORDER BY rating DESC;
