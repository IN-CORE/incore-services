import {connect} from "react-redux";
import DataViewerComponent from "../components/DataViewer";
import {fetchDatasets, searchDatasets} from "../actions";

const mapStateToProps = (state, ownProps) => {
	return {
		datasets: state.data.datasets,
		authError: state.user.loginError,
		locationFrom: state.user.locationFrom
	};
};

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		getAllDatasets: (limit, offset, dataType) => {
			dispatch(fetchDatasets(limit, offset, dataType));
		},
		searchAllDatasets: (limit, offset, keyword) => {
			dispatch(searchDatasets(limit, offset, keyword));
		}
	};
};

const DataViewer = connect(mapStateToProps, mapDispatchToProps)(DataViewerComponent);

export default DataViewer;
