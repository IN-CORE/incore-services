import {connect} from "react-redux";
import AppComponent from "../components/App";
import {logout} from "../actions";

const mapStateToProps = (state,ownProps) => {
	return{
		user: sessionStorage.user
	};
};

const mapDispatchToProps = (dispatch, ownProps) => {
	return {
		logout: () => {
			dispatch(logout());
		}
	};
};


const App = connect(mapStateToProps, mapDispatchToProps)(AppComponent);

export default App;
