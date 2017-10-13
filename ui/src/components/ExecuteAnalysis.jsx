import React, {Component} from "react";
import {Textfield, Display2, Button} from "react-mdc-web";
import AnalysisSelect from "../containers/AnalysisSelect";

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

	}

	render() {
		let contents;
		if(this.props.analysis !== null) {
			const inputs = this.props.analysis.datasets.map( input =>
				<span key={input.name}> <Textfield floatingLabel={input.name} id={input.id} value={this.state.inputs.id} width="100%"/> <br/></span>
			);
			const parameters = this.props.analysis.parameters.map(param =>
				<span key={param.name} > <Textfield floatingLabel={param.name} id={param.id} value={this.state.parameters.id} /><br/></span>
			);

			contents = <div>
				<h1>{this.props.analysis.name}</h1>
				<h3>Inputs</h3>
				{inputs}
				<h3> Parameters</h3>
				{parameters}
				<br/>
				<br/>
				<Button raised primary onClick={this.executeAnalysis}> Execute Analysis </Button>
			</div>;
		}


		return (
			<div className="main">
				<Display2 className="center"> Execute Analysis</Display2>
				Select an Analysis to execute <br/>
				<AnalysisSelect/>
				{contents}
			</div>
		);
	}

}

export default ExecuteAnalysis;
