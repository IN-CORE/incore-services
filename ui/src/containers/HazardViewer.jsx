import {connect} from "react-redux";
import HazardExplorer from "../components/HazardExplorerPage";
import {fetchHazards, fetchSpaces, searchHazards} from "../actions";

const mapStateToProps = (state, ownProps) => {
	return {
		hazards: state.hazard.hazards,
		spaces: state.space.spaces,
		authError: state.user.loginError,
		locationFrom: state.user.locationFrom
	};
};

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		getAllSpaces: () =>{
			dispatch(fetchSpaces());
		},
		getAllHazards: (hazard_type, space, limit, offset) => {
			dispatch(fetchHazards(hazard_type, space, limit, offset));
		},
		searchAllHazards: (hazard_type, keyword, limit, offset) => {
			dispatch(searchHazards(hazard_type, keyword, limit, offset));
		}
	};
};

const HazardViewer = connect(mapStateToProps, mapDispatchToProps)(HazardExplorer);

export default HazardViewer;
