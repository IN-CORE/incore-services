import {connect} from "react-redux";
import {fetchAnalyses} from "../actions/index";
import AppComponent from "../components/App";
import type { Dispatch } from "../utils/flowtype";

const mapDispatchToProps = (dispatch: Dispatch) => {
	return {
		loadAnalyses: () => {
			dispatch(fetchAnalyses());
		}
	};
};

const App = connect(null, mapDispatchToProps)(AppComponent);

export default App;
