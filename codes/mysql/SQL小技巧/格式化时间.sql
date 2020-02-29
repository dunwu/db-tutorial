USE fide_0_0_6;

-- 查询时间，友好提示
SELECT date_format(t.judgement_start_time, '%Y-%m-%d') AS day
FROM t_judgement_log t;

-- int 时间戳类型
SELECT from_unixtime(t.judgement_start_time, '%Y-%m-%d') AS day
FROM t_judgement_log t;

EXPLAIN
SELECT *
FROM t_judgement_log t WHERE t.judgement_id > 1000;


EXPLAIN
SELECT *
FROM t_metric_template t
WHERE t.id = '1c4cab216a5e449688960536cc069b96';
