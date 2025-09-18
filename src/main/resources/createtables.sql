USE [hhparser5]
GO

/****** Object:  Table [dbo].[employer]    Script Date: 10.08.2022 12:55:02 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[employer](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[hhid] [varchar](20) NULL,
	[name] [nvarchar](max) NULL,
 CONSTRAINT [PK_employer] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO


-------------------------------------------------------------------------

USE [hhparser5]
GO

/****** Object:  Table [dbo].[project]    Script Date: 10.08.2022 12:55:42 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[project](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](max) NOT NULL,
 CONSTRAINT [PK_project] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO


-------------------------------------------------------------------------
USE [hhparser5]
GO

/****** Object:  Table [dbo].[project_search_text]    Script Date: 10.08.2022 12:56:15 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[project_search_text](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[project_id] [bigint] NULL,
	[name] [nvarchar](max) NULL,
 CONSTRAINT [PK_project_search_text] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[project_search_text]  WITH CHECK ADD  CONSTRAINT [FK_project_search_text_project] FOREIGN KEY([project_id])
REFERENCES [dbo].[project] ([id])
GO

ALTER TABLE [dbo].[project_search_text] CHECK CONSTRAINT [FK_project_search_text_project]
GO
-------------------------------------------------------------------------
USE [hhparser5]
GO

/****** Object:  Table [dbo].[vacancy]    Script Date: 10.08.2022 12:56:59 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[vacancy](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[hhid] [varchar](20) NULL,
	[name] [nvarchar](max) NULL,
	[employer_id] [bigint] NOT NULL,
	[salary_from] [int] NULL,
	[salary_to] [int] NULL,
	[gross] [smallint] NULL,
	[url] [varchar](max) NULL,
	[alternate_url] [varchar](max) NULL,
	[archived] [smallint] NULL,
 CONSTRAINT [PK_vacancy] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

-------------------------------------------------------------------------
USE [hhparser5]
GO

/****** Object:  Table [dbo].[publication_history]    Script Date: 10.08.2022 12:57:20 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[publication_history](
	[date_published] [date] NULL,
	[project_id] [bigint] NULL,
	[vacancy_id] [bigint] NULL,
	[hhid] [varchar](20) NULL,
	[date_closed] [date] NULL,
	[logmoment] [datetime] NULL
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[publication_history]  WITH CHECK ADD  CONSTRAINT [FK_publication_history_project] FOREIGN KEY([project_id])
REFERENCES [dbo].[project] ([id])
GO

ALTER TABLE [dbo].[publication_history] CHECK CONSTRAINT [FK_publication_history_project]
GO

ALTER TABLE [dbo].[publication_history]  WITH CHECK ADD  CONSTRAINT [FK_publication_history_vacancy] FOREIGN KEY([vacancy_id])
REFERENCES [dbo].[vacancy] ([id])
GO

ALTER TABLE [dbo].[publication_history] CHECK CONSTRAINT [FK_publication_history_vacancy]
GO


-------------------------------------------------------------------------
USE [hhparser5]
GO

/****** Object:  Table [dbo].[search_history]    Script Date: 10.08.2022 12:58:03 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[search_history](
	[search_date] [date] NULL,
	[project_id] [bigint] NULL,
	[vacancy_id] [bigint] NULL,
	[hhid] [varchar](20) NULL
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[search_history]  WITH CHECK ADD  CONSTRAINT [FK_search_history_project] FOREIGN KEY([project_id])
REFERENCES [dbo].[project] ([id])
GO

ALTER TABLE [dbo].[search_history] CHECK CONSTRAINT [FK_search_history_project]
GO

ALTER TABLE [dbo].[search_history]  WITH CHECK ADD  CONSTRAINT [FK_search_history_vacancy] FOREIGN KEY([vacancy_id])
REFERENCES [dbo].[vacancy] ([id])
GO

ALTER TABLE [dbo].[search_history] CHECK CONSTRAINT [FK_search_history_vacancy]
GO


USE [hhparser5]
GO

/****** Object:  Table [dbo].[vacancy_source]    Script Date: 17.01.2023 13:14:42 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[vacancy_source](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[hhid] [varchar](20) NULL,
	[json] [nvarchar](max) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO


