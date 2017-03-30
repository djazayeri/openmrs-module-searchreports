package org.openmrs.module.searchreports;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSetMetaData;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;

public class FlattenDataSetWithDatesDataSetEvaluatorTest {
	
	@Test
	public void testEvaluate() throws Exception {
		EvaluationContext evaluationContext = mock(EvaluationContext.class);
		DataSetDefinitionService dataSetDefinitionService = mock(DataSetDefinitionService.class);
		
		FlattenDataSetWithDatesDataSetEvaluator evaluator = new FlattenDataSetWithDatesDataSetEvaluator(
				dataSetDefinitionService);
		
		EncounterDataSetDefinition inputDsd = new EncounterDataSetDefinition();
		
		DataSetColumn ptId = new DataSetColumn("patient_id", "patient_id", Integer.class);
		DataSetColumn encDate = new DataSetColumn("encounter_datetime", "encounter_datetime", Date.class);
		DataSetColumn wt = new DataSetColumn("wt", "wt", Double.class);
		DataSetColumn foodCode = new DataSetColumn("food", "food_code", Integer.class);
		
		SimpleDataSetMetaData metaData = new SimpleDataSetMetaData();
		metaData.addColumn(ptId);
		metaData.addColumn(encDate);
		metaData.addColumn(wt);
		metaData.addColumn(foodCode);
		
		SimpleDataSet input = new SimpleDataSet(inputDsd, evaluationContext);
		input.setMetaData(metaData);
		
		DataSetRow row1 = new DataSetRow();
		row1.addColumnValue(ptId, 7);
		row1.addColumnValue(encDate, DateUtil.parseYmd("2017-01-01"));
		row1.addColumnValue(wt, 71.2);
		row1.addColumnValue(foodCode, 364);
		input.addRow(row1);
		
		DataSetRow row2 = new DataSetRow();
		row2.addColumnValue(ptId, 7);
		row2.addColumnValue(encDate, DateUtil.parseYmd("2017-02-04"));
		row2.addColumnValue(wt, 72.3);
		row2.addColumnValue(foodCode, 365);
		input.addRow(row2);
		
		Mapped<DataSetDefinition> baseDefinition = Mapped.<DataSetDefinition>noMappings(inputDsd);
		
		when(dataSetDefinitionService.evaluate(baseDefinition, evaluationContext)).thenReturn(input);
		
		FlattenDataSetWithDatesDataSetDefinition definition = new FlattenDataSetWithDatesDataSetDefinition();
		definition.setBaseDefinition(baseDefinition);
		definition.setSortByColumn("encounter_datetime");
		definition.setJoinOnColumn("patient_id");
		
		SimpleDataSet evaluated = (SimpleDataSet) evaluator.evaluate(definition, evaluationContext);
		
		assertThat(evaluated.getMetaData().getColumnCount(), is(7));
		assertThat(evaluated.getMetaData().getColumns().get(0).getName(), is("patient_id"));
		assertThat(evaluated.getMetaData().getColumns().get(1).getName(), is("1_encounter_datetime"));
		assertThat(evaluated.getMetaData().getColumns().get(2).getName(), is("1_wt"));
		assertThat(evaluated.getMetaData().getColumns().get(3).getName(), is("1_food"));
		assertThat(evaluated.getMetaData().getColumns().get(4).getName(), is("2_encounter_datetime"));
		assertThat(evaluated.getMetaData().getColumns().get(5).getName(), is("2_wt"));
		assertThat(evaluated.getMetaData().getColumns().get(6).getName(), is("2_food"));
		
		assertThat(evaluated.getRows().size(), is(1));
		DataSetRow row = evaluated.getRows().get(0);
		assertThat((Integer) row.getColumnValue("patient_id"), is(7));
		assertThat((Date) row.getColumnValue("1_encounter_datetime"), is(DateUtil.parseYmd("2017-01-01")));
		assertThat((Double) row.getColumnValue("1_wt"), is(71.2));
		assertThat((Integer) row.getColumnValue("1_food"), is(364));
		assertThat((Date) row.getColumnValue("2_encounter_datetime"), is(DateUtil.parseYmd("2017-02-04")));
		assertThat((Double) row.getColumnValue("2_wt"), is(72.3));
		assertThat((Integer) row.getColumnValue("2_food"), is(365));
	}
	
}
