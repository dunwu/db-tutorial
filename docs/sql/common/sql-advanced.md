# 高级 SQL

> 不同于 [SQL Cheat Sheet](sql-cheat-sheet.md) 中的一般语法，本文主要整理收集一些高级但是很有用的 SQL

## 数据库

## 表

查看表的基本信息

```sql
SELECT * FROM information_schema.tables
WHERE table_schema = 'test' AND table_name = 'user';
```

查看表的列信息

```sql
SELECT * FROM information_schema.columns
WHERE table_schema = 'test' AND table_name = 'user';
```

## 参考资料

- [《SQL 必知必会》](https://item.jd.com/11232698.html)
