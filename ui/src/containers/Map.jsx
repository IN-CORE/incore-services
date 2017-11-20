import {connect} from "react-redux";
import MapComponent from "../components/Map";
import {} from "../actions";

const mapStateToProps = (state, ownProps) => {
	return {
		datasetId: state.execution.outputDatasetId
	};
};

const Map = connect(mapStateToProps)(MapComponent);

export default Map;
