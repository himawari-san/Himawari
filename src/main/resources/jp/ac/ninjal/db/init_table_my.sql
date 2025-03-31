CREATE TABLE IF NOT EXISTS corpus (
 id INT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS external_annotation (
 _id INT PRIMARY KEY AUTO_INCREMENT,
 _corpus_id INT,
 _start INT,
 _end INT
);

CREATE TABLE IF NOT EXISTS manual_annotation (
 id INT PRIMARY KEY AUTO_INCREMENT,
 corpus_id INT,
 start INT,
 end INT
);

