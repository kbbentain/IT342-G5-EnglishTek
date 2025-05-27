-- First drop the foreign key constraints that reference the quizzes table
ALTER TABLE quiz_attempts DROP FOREIGN KEY FKfwipvfipnnwsoacoyv5k7fbxc;
ALTER TABLE quiz_questions DROP FOREIGN KEY FKanfmgf6ksbdnv7ojb0pfve54q;

-- Convert quizzes table to InnoDB
ALTER TABLE quizzes ENGINE = InnoDB;

-- Recreate the foreign key constraints
ALTER TABLE quiz_attempts
  ADD CONSTRAINT FKfwipvfipnnwsoacoyv5k7fbxc 
  FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE quiz_questions
  ADD CONSTRAINT FKanfmgf6ksbdnv7ojb0pfve54q 
  FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE ON UPDATE CASCADE;
