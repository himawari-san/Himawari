CREATE TABLE IF NOT EXISTS corpus (
 corpus_id INT PRIMARY KEY AUTO_INCREMENT,
 corpus_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS field (
 field_id INT PRIMARY KEY AUTO_INCREMENT,
 field_name VARCHAR(255),
);

CREATE TABLE IF NOT EXISTS annotation (
 annotation_id INT PRIMARY KEY AUTO_INCREMENT,
 field_id INT,
 corpus_id INT,
 annotation VARCHAR(255),
 search_key VARCHAR(1000),
 start INT,
 end INT
);
create index on annotation(search_key);
create index on annotation(annotation);


ALTER TABLE annotation ADD CONSTRAINT IF NOT EXISTS FK_annotation_0 FOREIGN KEY (field_id) REFERENCES field (field_id);
ALTER TABLE annotation ADD CONSTRAINT IF NOT EXISTS FK_annotation_1 FOREIGN KEY (corpus_id) REFERENCES corpus (corpus_id);
