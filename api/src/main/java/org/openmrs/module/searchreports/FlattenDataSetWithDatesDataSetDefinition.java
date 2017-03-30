package org.openmrs.module.searchreports;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;

public class FlattenDataSetWithDatesDataSetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Mapped<DataSetDefinition> baseDefinition;
	
	@ConfigurationProperty
	private String joinOnColumn;
	
	@ConfigurationProperty
	private String sortByColumn;
	
	public Mapped<DataSetDefinition> getBaseDefinition() {
		return baseDefinition;
	}
	
	public void setBaseDefinition(Mapped<DataSetDefinition> baseDefinition) {
		this.baseDefinition = baseDefinition;
	}
	
	public String getJoinOnColumn() {
		return joinOnColumn;
	}
	
	public void setJoinOnColumn(String joinOnColumn) {
		this.joinOnColumn = joinOnColumn;
	}
	
	public String getSortByColumn() {
		return sortByColumn;
	}
	
	public void setSortByColumn(String sortByColumn) {
		this.sortByColumn = sortByColumn;
	}
}
