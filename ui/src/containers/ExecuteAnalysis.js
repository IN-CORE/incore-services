import {connect} from "react-redux";
import {fetchAnalyses, executeDatawolfWorkflow, fetchDatasets} from "../actions/index";
import ExecuteAnalysisComponent from "../components/ExecuteAnalysis";
import type { Dispatch } from "../utils/flowtype";

const mapStateToProps = (state, ownProps) => {
	return {
		analysis: state.analyses.selectedAnalysis,
		datasets: state.data.datasets,
		executionId:  state.execution.executionId,
	};
};


const mapDispatchToProps = (dispatch: Dispatch) => {
	return {
		loadAnalyses: () => {
			dispatch(fetchAnalyses());
		},
		executeAnalysis: async (workflowid, creatorid, title, description, parameters, datasets) => {
			await dispatch(executeDatawolfWorkflow(workflowid, creatorid, title, description, parameters, datasets));
		},
		loadDatasets: () => {
			dispatch(fetchDatasets());
		}
	};
};

const ExecuteAnalysis = connect(mapStateToProps, mapDispatchToProps)(ExecuteAnalysisComponent);

export default ExecuteAnalysis;
