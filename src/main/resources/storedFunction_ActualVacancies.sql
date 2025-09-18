USE [hhparser5]
GO

/****** Object:  UserDefinedFunction [dbo].[vacsListOrderedBySalaryNetto]    Script Date: 31.08.2022 17:21:59 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


-- =============================================
-- Author:		<Author,,Name>
-- Create date: <Create Date,,>
-- Description:	<Description,,>
-- =============================================
create FUNCTION [dbo].[vacsListOrderedBySalaryNetto]
(	
	-- Add the parameters for the function here
	@project_id bigint, 
	@date_published Date
)
RETURNS TABLE 
AS
RETURN 
(
	-- Add the SELECT statement with parameter references here
/*Declare @project_id bigint;
Declare @date_published Date;
SET @project_id = 2;
SET @date_published = '2022-08-08';
*/
    SELECT 
      h.vacancy_id, 
      CASE WHEN v.salary_to > 0 THEN CASE WHEN v.gross = 1 THEN v.salary_to * 0.87 ELSE salary_to END ELSE CASE WHEN v.gross = 1 THEN v.salary_from * 0.87 ELSE salary_from END END AS salary_netto, 
	  v.hhid as hhid, 
	  v.NAME AS vacancy_name, 
	  e.NAME AS employer_name, 
	  e.hhid AS employer_hhid, 
	  v.alternate_url as alternate_url
    FROM 
      (
        SELECT 
          Max(date_published) AS date_published, 
          vacancy_id, 
          project_id 
        FROM 
          publication_history AS h 
        WHERE 
          (date_published <= @date_published) 
          AND (project_id = @project_id) 
          --AND (date_closed IS NULL) 
        GROUP BY 
          vacancy_id, 
          project_id
      ) AS subq 
      INNER JOIN publication_history AS h ON h.date_published = subq.date_published 
      AND h.vacancy_id = subq.vacancy_id 
      AND h.project_id = subq.project_id 
      LEFT OUTER JOIN vacancy AS v ON v.id = h.vacancy_id
	  LEFT OUTER JOIN employer AS e ON e.id = v.employer_id 
	WHERE
		(h.date_closed IS NULL)
		OR (h.date_closed > @date_published )
  
)
GO

-- using example

USE [hhparser5]
GO

SELECT * FROM [dbo].[vacsListOrderedBySalaryNetto] (
   1
  ,'2032-01-01')

  where employer_name like '%сбер%'

order by salary_netto desc, employer_name
