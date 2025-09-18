Vacancies list
Плоский список с колонками:
Вакансия - Работодатель - МАКС (зарплата от, зарплата до)(netto)
Формируется по таблице Publication history, как срез последних плюс фильтр по статусу:
" = Открыта" (_closed = 0)
(статус - это ресурс)


SELECT * FROM [dbo].[vacsListOrderedBySalaryNetto] (
   1 --project id
  ,     '2099-01-01'
)
order by salary_netto desc, employer_name asc
GO




---
2022-08-26
Запрос для отображения новых на сегодня вакансий
(относительно вчера, т.е. Today - 1 day)
Выбираем 2 множества - Today, Yesterday
Затем оставляем только то, что есть сегодня, а вчера этого не было (NULL)

--new for today
Declare @Today date;
SET @Today = getdate();
Declare @Yesterday date;
SET @Yesterday =  dateadd(DAY,-1,@Today) ;
Declare @ProjectID bigint;
SET @ProjectID = 1;
Declare @SliceToday table (vacancy_id bigint, hhid varchar(10), salary_netto decimal);
Declare @SlicePrev table (vacancy_id bigint, hhid varchar(10), salary_netto decimal);
insert into @SliceToday select 
vacancy_id , hhid , salary_netto 
FROM [dbo].[vacsListOrderedBySalaryNetto] (
   @ProjectID
  ,@Today);

insert into @SlicePrev select 
vacancy_id , hhid , salary_netto 
FROM [dbo].[vacsListOrderedBySalaryNetto] (
   @ProjectID
  ,@Yesterday);
select v.name, tod.salary_netto, e.name, v.alternate_url from @SliceToday as tod
left join @SlicePrev as prev
on prev.vacancy_id = tod.vacancy_id
left join vacancy as v on v.id = tod.vacancy_id
left join employer as e on e.id = v.employer_id
where prev.hhid is null
order by salary_netto desc

---
Запрос для отображения закрытых сегодня вакансий

--closed today
Declare @Today date;
SET @Today = getdate();
Declare @ProjectID bigint;
SET @ProjectID = 1;

select v.name
, case when v.gross = 1 then v.salary_to*0.87 else v.salary_to end as salary_netto
, e.name, v.alternate_url from 
[dbo].[publication_history] as h
left join vacancy as v on v.id = h.vacancy_id
left join employer as e on e.id = v.employer_id
where h.date_closed = @Today
order by salary_netto desc

---
Переоткрытые вакансии (сегодня)
Связь по работодателю и только!
Поэтому вакансии будут дублироваться, если их было более одной (декартово произведение)

--reopened today
Declare @Today date;
SET @Today = getdate();
Declare @Yesterday date;
SET @Yesterday =  dateadd(DAY,-1,@Today) ;
Declare @ProjectID bigint;
SET @ProjectID = 1;
Declare @SliceToday table (vacancy_id bigint, hhid varchar(10), salary_netto decimal);
Declare @SlicePrev table (vacancy_id bigint, hhid varchar(10), salary_netto decimal);

insert into @SliceToday select 
vacancy_id , hhid , salary_netto 
FROM [dbo].[vacsListOrderedBySalaryNetto] (
   @ProjectID
  ,@Today);

insert into @SlicePrev select 
vacancy_id , hhid , salary_netto 
FROM [dbo].[vacsListOrderedBySalaryNetto] (
   @ProjectID
  ,@Yesterday);


Select 
	v_new.name as name_new
	, closed_today.salary_netto as salary_old
	, created_today.salary_netto as salary_new
	, e.name as employer
	, v_old.alternate_url as url_old
	, v_new.alternate_url as url_new
from 
(select tod.vacancy_id, tod.salary_netto, e.id as emp_id 
from 
@SliceToday as tod
left join @SlicePrev as prev
on prev.vacancy_id = tod.vacancy_id
left join vacancy as v on v.id = tod.vacancy_id
left join employer as e on e.id = v.employer_id
where prev.hhid is null) as created_today
left join 
(select h.vacancy_id
, case when v.gross = 1 then v.salary_to*0.87 else v.salary_to end as salary_netto
, e.id as emp_id from 
[dbo].[publication_history] as h
left join vacancy as v on v.id = h.vacancy_id
left join employer as e on e.id = v.employer_id
where h.date_closed = @Today
) as closed_today
on created_today.emp_id = closed_today.emp_id
left join vacancy as v_old on v_old.id = closed_today.vacancy_id
left join vacancy as v_new on v_new.id = created_today.vacancy_id
left join employer as e on e.id = v_new.employer_id
where closed_today.vacancy_id is not null
order by salary_new desc