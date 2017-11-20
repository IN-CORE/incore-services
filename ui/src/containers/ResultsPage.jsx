import {connect} from "react-redux";
import ResultsPageComponent from "../components/ResultsPage";
import {getOutputDataset} from "../actions";

const mapStateToProps = (state, ownProps) => {
	return {
		analysis: state.analyses.selectedAnalysis,
		executionId: state.execution.executionId,
		fileData: state.execution.outputFile
	};
};

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		getOutputFile: (event) => {
			dispatch(getOutputDataset());
		}
	};
};

const ResultsPage = connect(mapStateToProps, mapDispatchToProps)(ResultsPageComponent);

export default ResultsPage;
