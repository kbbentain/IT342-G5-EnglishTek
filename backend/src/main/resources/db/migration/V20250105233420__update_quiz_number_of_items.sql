UPDATE quizzes q
SET number_of_items = (
    SELECT COUNT(*)
    FROM quiz_questions qq
    WHERE qq.quiz_id = q.id
);
