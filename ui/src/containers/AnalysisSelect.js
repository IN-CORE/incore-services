import {connect} from "react-redux";
import AnalysisSelectComponent from "../components/AnalysisSelect";
import {} from "../actions/index";

const mapStateToProps = (state, ownProps) => {
	return {
		analyses: state.analyses.analysesMetadata
	};
};

// const mapDispatchToProps = (dispatch, ownProps) => {
// 	return {
// 		onChangex: (event, valy) => {
// 			dispatch(actionName(valy));
// 		}
// 	}
// };

const AnalysisSelect = connect(mapStateToProps)(AnalysisSelectComponent);

export default AnalysisSelect;
