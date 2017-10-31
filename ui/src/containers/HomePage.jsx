import {connect} from "react-redux";
import HomePageComponent from "../components/HomePage";
import {login} from "../actions";

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		login: (username, password) => {
			dispatch(login(username,password));
		}
	};
};

const HomePage = connect(null, mapDispatchToProps)(HomePageComponent);

export default HomePage;
