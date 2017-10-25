import React, {Component} from "react";
import {browserHistory} from "react-router";
import AnalysisSelect from "../containers/AnalysisSelect";
import {TextField, RaisedButton} from "material-ui";

class ExecuteAnalysis extends Component {

	constructor(props) {
		super(props);
		this.state = {
			inputs: [],
			parameters: []
		};
		this.executeAnalysis = this.executeAnalysis.bind(this);
	}

	componentWillMount() {
		this.props.loadAnalyses();
	}

	executeAnalysis(event) {
		//TODO: Login using a global state action
		browserHistory.push("/Results");
	}

	render() {
		let contents;
		if(this.props.analysis !== null) {
			const inputs = this.props.analysis.datasets.map( input =>
				<span key={input.name}> <TextField floatingLabelText={input.name} id={input.id} value={this.state.inputs.id} width="100%"/> <br/></span>
			);
			const parameters = this.props.analysis.parameters.map(param =>
				<span key={param.name} > <TextField floatingLabelText={param.name} id={param.id} value={this.state.parameters.id} /><br/></span>
			);

			contents = <div>
				<h1>{this.props.analysis.name}</h1>
				<h3>Inputs</h3>
				{inputs}
				<h3> Parameters</h3>
				{parameters}
				<br/>
				<br/>
				<RaisedButton primary onClick={this.executeAnalysis} label="Execute Analysis"/>
			</div>;
		}


		return (
			<div className="main">
				<h2 className="center"> Execute Analysis</h2>
				Select an Analysis to execute <br/>
				<AnalysisSelect/>
				{contents}
			</div>
		);
	}

}

export default ExecuteAnalysis;
