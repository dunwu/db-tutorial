#     重新格式化部门表
#
#     @link https://leetcode-cn.com/problems/reformat-department-table/
#
#     部门表 Department：
#
#     +---------------+---------+
#     | Column Name   | Type    |
#     +---------------+---------+
#     | id            | int     |
#     | revenue       | int     |
#     | month         | varchar |
#     +---------------+---------+
#     (id, month) 是表的联合主键。
#     这个表格有关于每个部门每月收入的信息。
#     月份（month）可以取下列值 ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]。
#      
#
#     编写一个 SQL 查询来重新格式化表，使得新的表中有一个部门 id 列和一些对应 每个月 的收入（revenue）列。
#
#     查询结果格式如下面的示例所示：
#
#     Department 表：
#     +------+---------+-------+
#     | id   | revenue | month |
#     +------+---------+-------+
#     | 1    | 8000    | Jan   |
#     | 2    | 9000    | Jan   |
#     | 3    | 10000   | Feb   |
#     | 1    | 7000    | Feb   |
#     | 1    | 6000    | Mar   |
#     +------+---------+-------+
#
#     查询得到的结果表：
#     +------+-------------+-------------+-------------+-----+-------------+
#     | id   | Jan_Revenue | Feb_Revenue | Mar_Revenue | ... | Dec_Revenue |
#     +------+-------------+-------------+-------------+-----+-------------+
#     | 1    | 8000        | 7000        | 6000        | ... | null        |
#     | 2    | 9000        | null        | null        | ... | null        |
#     | 3    | null        | 10000       | null        | ... | null        |
#     +------+-------------+-------------+-------------+-----+-------------+
#
#     注意，结果表有 13 列 (1个部门 id 列 + 12个月份的收入列)。

USE db_tutorial;

CREATE TABLE IF NOT EXISTS department (
    id      INT,
    revenue INT,
    month   VARCHAR(20)
);

INSERT INTO department
VALUES (1, 8000, 'Jan');
INSERT INTO department
VALUES (2, 9000, 'Jan');
INSERT INTO department
VALUES (3, 10000, 'Feb');
INSERT INTO department
VALUES (1, 7000, 'Feb');
INSERT INTO department
VALUES (1, 6000, 'Mar');

-- 解题

SELECT id, revenue AS jan_revenue
FROM department
WHERE month = 'Jan';

SELECT id,
    SUM(CASE month WHEN 'Jan' THEN revenue END) jan_revenue,
    SUM(CASE month WHEN 'Feb' THEN revenue END) feb_revenue,
    SUM(CASE month WHEN 'Mar' THEN revenue END) mar_revenue,
    SUM(CASE month WHEN 'Apr' THEN revenue END) apr_revenue,
    SUM(CASE month WHEN 'May' THEN revenue END) may_revenue,
    SUM(CASE month WHEN 'Jun' THEN revenue END) jun_revenue,
    SUM(CASE month WHEN 'Jul' THEN revenue END) jul_revenue,
    SUM(CASE month WHEN 'Aug' THEN revenue END) aug_revenue,
    SUM(CASE month WHEN 'Sep' THEN revenue END) sep_revenue,
    SUM(CASE month WHEN 'Oct' THEN revenue END) oct_revenue,
    SUM(CASE month WHEN 'Nov' THEN revenue END) nov_revenue,
    SUM(CASE month WHEN 'Dec' THEN revenue END) dec_revenue
FROM department
GROUP BY id;

SELECT id,
    SUM(IF(month = 'Jan', revenue, NULL)) jan_revenue,
    SUM(IF(month = 'Feb', revenue, NULL)) feb_revenue,
    SUM(IF(month = 'Mar', revenue, NULL)) mar_revenue,
    SUM(IF(month = 'Apr', revenue, NULL)) apr_revenue,
    SUM(IF(month = 'May', revenue, NULL)) may_revenue,
    SUM(IF(month = 'Jun', revenue, NULL)) jun_revenue,
    SUM(IF(month = 'Jul', revenue, NULL)) jul_revenue,
    SUM(IF(month = 'Aug', revenue, NULL)) aug_revenue,
    SUM(IF(month = 'Sep', revenue, NULL)) sep_revenue,
    SUM(IF(month = 'Oct', revenue, NULL)) oct_revenue,
    SUM(IF(month = 'Nov', revenue, NULL)) nov_revenue,
    SUM(IF(month = 'Dec', revenue, NULL)) dec_revenue
FROM department
GROUP BY id;
