import {connect} from "react-redux";
import HazardExplorer from "../components/HazardExplorerPage";
import {fetchHazards, searchHazards} from "../actions";

const mapStateToProps = (state, ownProps) => {
	return {
		hazards: state.hazard.hazards,
		authError: state.user.loginError,
		locationFrom: state.user.locationFrom
	};
};

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		getAllHazards: (hazard_type, limit, offset) => {
			dispatch(fetchHazards(hazard_type, limit, offset));
		},
		searchAllHazards: (limit, offset, keyword) => {
			dispatch(searchHazards(limit, offset, keyword));
		}
	};
};

const HazardViewer = connect(mapStateToProps, mapDispatchToProps)(HazardExplorer);

export default HazardViewer;
