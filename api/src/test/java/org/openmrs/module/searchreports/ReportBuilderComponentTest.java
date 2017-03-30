package org.openmrs.module.searchreports;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportBuilderComponentTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private ReportBuilder reportBuilder;
	
	@Test
	public void testBuildingDataset() throws Exception {
		DataSetDefinition dsd = reportBuilder.buildFlattenedDataSetDefinition();
		DataSet output = DefinitionContext.getDataSetDefinitionService().evaluate(dsd, new EvaluationContext());
		// underlying data has 3 encounters (I think with ids 3,4,5 in standardTestDataset.xml)
		assertThat(output.getMetaData().getColumnCount(), is(13));
		printDatasetToSystemOut(output);
	}
	
	private void printDatasetToSystemOut(DataSet ds) {
		List<DataSetColumn> cols = ds.getMetaData().getColumns();
		for (DataSetColumn col : cols) {
			System.out.print(col.getLabel());
			System.out.print("\t");
		}
		System.out.println();
		for (DataSetRow row : ds) {
			for (DataSetColumn col : cols) {
				System.out.print(row.getColumnValue(col));
				System.out.print("\t");
			}
			System.out.println();
		}
		
	}
	
}
