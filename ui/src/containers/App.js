import {connect} from "react-redux";
import AppComponent from "../components/App";

const mapStateToProps = (state,ownProps) => {
	return{
		user: sessionStorage.user
	};
};

const App = connect(mapStateToProps)(AppComponent);

export default App;
