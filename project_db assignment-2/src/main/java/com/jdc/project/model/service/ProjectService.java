package com.jdc.project.model.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jdc.project.model.dto.Project;
import com.jdc.project.model.service.utils.ProjectHelper;

@Service
public class ProjectService {
	
	@Autowired
	private ProjectHelper projectHelper;
	
	@Autowired
	private SimpleJdbcInsert projectInsert;
	
	@Autowired
	private NamedParameterJdbcTemplate template;
	
	private RowMapper<Project> rowMapper;
	
	@Value("${service.project.findbyId}")
	 private String findbyId;
	
	@Value("${service.project.search}")
	private String searchstmt;
	
	@Value("${service.project.update}")
	private String updatestmt;
	
	@Value("${service.project.delete}")
	private String deletebyId;
	
	private String searchmanager =" where m.name like :manager";
	private String searchstartdate = " where p.start like :startDate";
	private String searchdateTo = " where p.start like :dateTo";
	
	public ProjectService() {
		rowMapper = new BeanPropertyRowMapper<>(Project.class);
	}
	
	public int create(Project project) {
		projectHelper.validate(project);		
		return projectInsert.executeAndReturnKey(projectHelper.insertParams(project)).intValue();
	}

	public Project findById(int id) {
		
		 return template.queryForObject(findbyId, Map.of("id", id), rowMapper);
	
	}

	public List<Project> search(String project, String manager, LocalDate dateFrom, LocalDate dateTo) {
		
		var sb = new StringBuffer(searchstmt);
		var params = new HashMap<String, Object>();
		
		if(null == project && null == manager && null == dateFrom && null == dateTo) {
			return template.query(searchstmt, rowMapper);
		}
		
		if (StringUtils.hasLength(manager)) {
			sb.append(searchmanager);
			params.put("manager", manager.concat("%"));
			return template.queryForStream(sb.toString(), params, rowMapper).map(a -> (Project)a).toList();
		}
		
		if ( dateFrom != null) {
			sb.append(searchstartdate);
			params.put("startDate", dateFrom.toString().concat("%"));
			return template.queryForStream(sb.toString(), params, rowMapper).map(a -> (Project)a).toList();
		}
		
		if (null != dateTo) {
			sb.append(searchdateTo);
			params.put("dateTo", dateTo.toString().concat("%"));
			return template.queryForStream(sb.toString(), params, rowMapper).map(a -> (Project)a).toList();
		}
		
		return null;
	}

	public int update(int id, String name, String description, LocalDate startDate, int month) {

		
		var param = new MapSqlParameterSource();
		param.addValue("name", name);
		param.addValue("description", description);
		param.addValue("start", Date.valueOf(startDate));
		param.addValue("months", month);
		param.addValue("id", id);
		
		return template.update(updatestmt, param);
	}

	public int deleteById(int id) {
		var param = new MapSqlParameterSource();
		param.addValue("id", id);
		
		return template.update(deletebyId, param);
	}

}
