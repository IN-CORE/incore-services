import {connect} from "react-redux";
import DataViewerComponent from "../components/DataViewer";
import {fetchDatasets, fetchSpaces, searchDatasets} from "../actions";


const mapStateToProps = (state, ownProps) => {
	return {
		datasets: state.data.datasets,
		spaces: state.space.spaces,
		authError: state.user.loginError,
		locationFrom: state.user.locationFrom
	};
};

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		getAllSpaces: () => {
			dispatch(fetchSpaces());
		},
		getAllDatasets: (dataType, space, limit, offset) => {
			dispatch(fetchDatasets(dataType, space, limit, offset));
		},
		searchAllDatasets: (keyword, limit, offset) => {
			dispatch(searchDatasets(keyword, limit, offset));
		}
	};
};

const DataViewer = connect(mapStateToProps, mapDispatchToProps)(DataViewerComponent);

export default DataViewer;
