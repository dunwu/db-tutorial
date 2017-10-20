# Sql 约束

SQL 约束用于规定表中的数据规则。

如果存在违反约束的数据行为，行为会被约束终止。

约束可以在创建表时规定（通过 CREATE TABLE 语句），或者在表创建之后规定（通过 ALTER TABLE 语句）。

CREATE TABLE + CONSTRAINT 语法

```sql
CREATE TABLE table_name
(
	column_name1 data_type(size) constraint_name,
	column_name2 data_type(size) constraint_name,
	column_name3 data_type(size) constraint_name,
	....
);
```

- **NOT NULL** - 指示某列不能存储 NULL 值。
- **UNIQUE** - 保证某列的每行必须有唯一的值。
- **PRIMARY KEY** - NOT NULL 和 UNIQUE 的结合。确保某列（或两个列多个列的结合）有唯一标识，有助于更容易更快速地找到表中的一个特定的记录。
- **FOREIGN KEY** - 保证一个表中的数据匹配另一个表中的值的参照完整性。
- **CHECK** - 保证列中的值符合指定的条件。
- **DEFAULT** - 规定没有给列赋值时的默认值。

## NOT NULL

NOT NULL 约束强制列不接受 NULL 值。

NOT NULL 约束强制字段始终包含值。这意味着，如果不向字段添加值，就无法插入新记录或者更新记录。

例：

```sql
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255)
);
```

## UNIQUE

UNIQUE 约束唯一标识数据库表中的每条记录。

UNIQUE 和 PRIMARY KEY 约束均为列或列集合提供了唯一性的保证。

PRIMARY KEY 约束拥有自动定义的 UNIQUE 约束。

请注意，每个表可以有多个 UNIQUE 约束，但是每个表只能有一个 PRIMARY KEY 约束。

### 创建表时的 UNIQUE 约束

```sql
-- Mysql方法
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255),
    UNIQUE (person_id)
)

-- Oracle方法1
CREATE TABLE person (
    person_id INT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255)
)

-- Oracle方法2
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255),
    CONSTRAINT uc_PersonId UNIQUE (person_id)
)
```

### 修改表时的 UNIQUE 约束

```sql
-- Mysql方法
ALTER TABLE person
ADD UNIQUE (person_id);

-- Oracle方法
ALTER TABLE person
ADD CONSTRAINT uc_PersonId UNIQUE (person_id)
```

### 撤销 UNIQUE 约束

```sql
-- Mysql方法
ALTER TABLE person
DROP INDEX person_id;

-- Oracle方法
ALTER TABLE person
DROP CONSTRAINT uc_PersonId;
```

## PRIMARY KEY

PRIMARY KEY 约束唯一标识数据库表中的每条记录。

主键必须包含唯一的值。

主键列不能包含 NULL 值。

每个表都应该有一个主键，并且每个表只能有一个主键。

### 创建表时的 PRIMARY KEY 约束

```sql
-- Mysql / Oracle 方法
CREATE TABLE person (
    person_id INT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255)
);

-- Oracle方法2
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255),
    CONSTRAINT pk_PersonId PRIMARY KEY (person_id)
)
```

### 修改表时的 PRIMARY KEY 约束

```sql
-- Mysql 方法
ALTER TABLE person
ADD PRIMARY KEY (person_id);

-- Oracle 方法
ALTER TABLE person
ADD CONSTRAINT pk_PersonId PRIMARY KEY (person_id);
```

### 销毁表时的 PRIMARY KEY 约束

```sql
-- Mysql 方法
ALTER TABLE person
DROP PRIMARY KEY;

-- Oracle 方法
ALTER TABLE person
DROP CONSTRAINT pk_PersonId;
```

## FOREIGN KEY

一个表中的 FOREIGN KEY 指向另一个表中的 PRIMARY KEY。

FOREIGN KEY 约束用于预防破坏表之间连接的行为。

FOREIGN KEY 约束也能防止非法数据插入外键列，因为它必须是它指向的那个表中的值之一。

### 创建表时的 FOREIGN KEY 约束

```sql
-- Mysql / Oracle 方法
CREATE TABLE bill
(
	bill_id INT NOT NULL,
	bill_no INT NOT NULL,
	person_id INT,
	PRIMARY KEY (bill_id),
	FOREIGN KEY (person_id) REFERENCES person(person_id)
);

-- Oracle 方法2
CREATE TABLE bill
(
	bill_id INT NOT NULL,
	bill_no INT NOT NULL,
	person_id INT,
	PRIMARY KEY (bill_id),
	CONSTRAINT fk_PersonId FOREIGN KEY (person_id) REFERENCES person(person_id)
);
```

### 修改表时的 FOREIGN KEY 约束

```sql
-- 方法1
ALTER TABLE bill
ADD FOREIGN KEY (person_id)
REFERENCES person(person_id);

-- 方法2
ALTER TABLE bill
ADD CONSTRAINT fk_PersonId
FOREIGN KEY (person_id)
REFERENCES person(person_id);
```

### 撤销表时的 FOREIGN KEY 约束

```sql
-- mysql 方法
ALTER TABLE Orders
DROP FOREIGN KEY fk_PersonId;

-- oracle 方法
ALTER TABLE Orders
DROP CONSTRAINT fk_PersonId;
```

## CHECK

CHECK 约束用于限制列中的值的范围。

如果对单个列定义 CHECK 约束，那么该列只允许特定的值。

如果对一个表定义 CHECK 约束，那么此约束会基于行中其他列的值在特定的列中对值进行限制。

### 创建表时的 CHECK 约束

```sql
-- Mysql / Oracle 方法
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT CHECK (age >= 18),
    address VARCHAR(255)
);

-- Oracle 方法2
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT,
    address VARCHAR(255),
    CONSTRAINT chk_Person CHECK (age >= 18)
);
```

### 修改表时的 CHECK 约束

```sql
-- Mysql 方法
ALTER TABLE person
ADD CHECK (age >= 18);

-- Oracle 方法
ALTER TABLE person
ADD CONSTRAINT chk_Person CHECK (age >= 18);
```

### 撤销表时的 CHECK 约束

```sql
-- Mysql 方法
ALTER TABLE person
DROP CONSTRAINT person_id;

-- Oracle 方法
ALTER TABLE person
DROP CHECK chk_Person;
```

## DEFAULT

DEFAULT 约束用于向列中插入默认值。

如果没有规定其他的值，那么会将默认值添加到所有的新记录。

### 创建表时的 DEFAULT 约束

```sql
-- Mysql / Oracle 方法
CREATE TABLE person (
    person_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INT DEFAULT 18,
    address VARCHAR(255)
);
```

### 修改表时的 DEFAULT 约束

```sql
-- Mysql 方法
ALTER TABLE person
ALTER age SET DEFAULT 0;

-- Oracle 方法
ALTER TABLE person
MODIFY age SET DEFAULT 0;
```

### 撤销表时的 DEFAULT 约束

```sql
-- Mysql 方法
ALTER TABLE person
ALTER age DROP DEFAULT;

-- Oracle 方法
ALTER TABLE person
MODIFY COLUMN age DROP DEFAULT;
```

## AUTO INCREMENT

Mysql 和 Oracle 的自增加方式不同

Mysql

```sql
CREATE TABLE person (
    person_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    age INT CHECK (age >= 18),
    address VARCHAR(255)
);
```

Oracle

Oracle 不支持

如果想要实现自增序列，需要使用 sequence。

```sql
CREATE SEQUENCE seq_person
MINVALUE 1
START WITH 1
INCREMENT BY 1
CACHE 10;
```
上面的代码创建一个名为 seq_person 的 sequence 对象，它以 1 起始且以 1 递增。该对象缓存 10 个值以提高性能。cache 选项规定了为了提高访问速度要存储多少个序列值。

要在 person 表中插入新记录，我们必须使用 nextval 函数（该函数从 seq_person 序列中取回下一个值）：

```sql
INSERT INTO person(id, name, age, address)
VALUES (seq_person.nextval, 'zhangsan', 20, 'Beijing');
```