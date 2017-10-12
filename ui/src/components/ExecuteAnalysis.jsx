import react, {Component} from "react";
import AnalysisSelect from "../containers/AnalysisSelect";

class ExecuteAnalysis extends Component {

	constructor(props) {
		super(props);
		this.state = {};
	}

	render() {
		return (
			<div>
				<h1> Execute Analysis</h1>
				<AnalysisSelect/>
			</div>
		);
	}

}

export default ExecuteAnalysis;
