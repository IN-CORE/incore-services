import {connect} from "react-redux";
import ResultsPageComponent from "../components/ResultsPage";
import {} from "../actions";

const mapStateToProps = (state, ownProps) => {
	return {
		analysis: state.analyses.selectedAnalysis,
		executionId: state.execution.executionId,
	};
};

// const mapDispatchToProps = (dispatch, ownProps) => {
// 	return {
// 		onChangex: (event, valy) => {
// 			dispatch(actionName(valy));
// 		}
// 	}
// };

const ResultsPage = connect(mapStateToProps)(ResultsPageComponent);

export default ResultsPage;
