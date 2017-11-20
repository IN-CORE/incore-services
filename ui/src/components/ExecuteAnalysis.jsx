import React, {Component} from "react";
import {browserHistory} from "react-router";
import AnalysisSelect from "../containers/AnalysisSelect";
import InputDropdown from "./InputDropdown";
import {TextField, RaisedButton, MenuOption, SelectField} from "material-ui";
import config from "../app.config";

class ExecuteAnalysis extends Component {

	constructor(props) {
		super(props);
		this.state = {
			inputs: [],
			parameters: [],
			title: "",
			description: ""
		};
		this.executeAnalysis = this.executeAnalysis.bind(this);
		this.changeTitle = this.changeTitle.bind(this);
		this.changeDataset = this.changeDataset.bind(this);
		this.changeDescription = this.changeDescription.bind(this);
	}

	componentWillMount() {
		this.props.loadAnalyses();
		this.props.loadDatasets();
	}

	async executeAnalysis(event) {
		//Post to Datawolf
		const workflowId = "ed303240-42f5-4d21-9af0-236de19e83da";
		const creatorId = "18aad9aa-6b33-4a8f-9452-2bbcf3fca110"; // Incore-dev@lists.ncsa.illinois.edu
		const parameters = {
			"ac82f88d-c8e2-4c24-cc1d-d5433b0690e6": `${config.hazardService  }59f3315ec7d30d4d6741b0bb`,
			"75a0e1bb-0b08-44ad-eb85-edc00d22fcee": config.dataServiceBase,
			"2d8f56b7-d111-4a43-ace5-01c775548b4b": "59f8d08dc7d30d278f25095d"
		};
		let datasets = {};
		if(this.state.inputs["Buildings"]){
			datasets["fd9d6267-a035-4d11-f191-d33fbc5cb8bd"]= this.state.inputs["Buildings"];
		}
		if(this.state.inputs["Mean Damage"]){
			datasets["d5c8b213-70aa-4316-d5af-89769dabd48f"]= this.state.inputs["Mean Damage"];
		}
		await this.props.executeAnalysis(workflowId, creatorId, this.state.title, this.state.description, parameters, datasets);

		browserHistory.push(`/Results/${this.props.executionId}`);
	}

	changeTitle(event){
		this.setState({title: event.target.value});
	}

	changeDataset(name, value) {
		let new_inputs = Object.assign({}, this.state.inputs);
		new_inputs[name] = value;
		this.setState({inputs: new_inputs});
	}

	changeDescription(event){
		this.setState({description: event.target.value});
	}
	render() {
		let contents;
		if(this.props.analysis !== null) {
			let inputs = this.props.analysis.datasets.map( input =>

			<InputDropdown input={input} key={input.name} options={this.props.datasets}
			               onChange={this.changeDataset} value={this.state.inputs[input.name]}/>
			);

			const parameters = this.props.analysis.parameters.map(param =>

				<span key={param.name} > <TextField floatingLabelText={param.name} id={param.id} value={this.state.parameters.id} /><br/></span>
			);

			contents = (<div>
				<h1>{this.props.analysis.name}</h1>
				<h3> Submission Details </h3>
				<TextField floatingLabelText="Analysis Title" value={this.state.title} onChange={this.changeTitle}/> <br/>
				<TextField floatingLabelText="Description" value={this.state.description} onChange={this.changeDescription}/>
				<h3>Inputs</h3>
				{inputs}
				<br/>
				<br/>
				<RaisedButton primary onClick={this.executeAnalysis} label="Execute Analysis"/>
			</div>);
		}


		return (
			<div className="main">
				<h1 className="center"> Execute Analysis</h1>
				<h2>Select an Analysis to execute </h2>
				<AnalysisSelect/>
				{contents}
			</div>
		);
	}

}

export default ExecuteAnalysis;
