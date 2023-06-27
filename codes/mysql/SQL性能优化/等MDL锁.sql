-- --------------------------------------------------------------------------------------
-- 函数操作影响索引效率示例
-- @author Zhang Peng
-- ----------------------------------------------------------------------------------------

CREATE TABLE t (
    id INT(11) NOT NULL AUTO_INCREMENT COMMENT 'Id',
    c  INT(11) DEFAULT NULL,
    PRIMARY KEY (id)
)
    ENGINE = InnoDB;

DELIMITER ;;
DROP PROCEDURE IF EXISTS init;
CREATE PROCEDURE init()
BEGIN
    DECLARE i INT;
    SET i = 1;
    WHILE(i <= 100000)
        DO
            INSERT INTO t VALUES (i, i);
            SET i = i + 1;
        END WHILE;
END;;
DELIMITER ;

CALL init();
