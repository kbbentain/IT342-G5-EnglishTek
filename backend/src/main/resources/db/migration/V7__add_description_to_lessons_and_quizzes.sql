-- Add description column to lessons table
ALTER TABLE lessons
ADD COLUMN description TEXT;

-- Add description column to quizzes table
ALTER TABLE quizzes
ADD COLUMN description TEXT;
