import {connect} from "react-redux";
import {fetchAnalyses} from "../actions/index";
import AppComponent from "../components/App";
import type { Dispatch } from "../utils/flowtype";

const mapStateToProps = (state,ownProps) => {
	return{
		user: state.user.username
	};
};

const App = connect(mapStateToProps)(AppComponent);

export default App;
