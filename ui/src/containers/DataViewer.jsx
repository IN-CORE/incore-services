import {connect} from "react-redux";
import DataViewerComponent from "../components/DataViewer";
import {fetchDatasets} from "../actions";

const mapStateToProps = (state, ownProps) => {
	return {
		datasets: state.data.datasets,
		authError: state.user.loginError
	};
};

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		getAllDatasets: () => {
			dispatch(fetchDatasets());
		}
	};
};

const DataViewer = connect(mapStateToProps, mapDispatchToProps)(DataViewerComponent);

export default DataViewer;
