import React, {Component} from "react";
import {browserHistory} from "react-router";
import AnalysisSelect from "../containers/AnalysisSelect";
import InputDropdown from "./InputDropdown";
import {TextField, RaisedButton, MenuOption, SelectField} from "material-ui";
import config from "../app.config";
let wait = (ms) => new Promise(resolve => setTimeout(resolve, ms));

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
		const workflowId = "c310f2c4-19d6-447d-8134-377ed48934ee";
		const creatorId = sessionStorage.user; //"18aad9aa-6b33-4a8f-9452-2bbcf3fca110"; // Incore-dev@lists.ncsa.illinois.edu
		const parameters = {
			"8c7b0e37-c1e7-4bcd-8ad3-d4dd5a0e0f22": "http://141.142.210.193:8888/hazard/api/earthquakes/59f3315ec7d30d4d6741b0bb",//`${config.hazardService  }59f3315ec7d30d4d6741b0bb`,
			"ab5370d9-5e61-4934-d081-705ec0ef6563": "http://141.142.210.193:8888/", //config.dataServiceBase,
			"e1296856-a9ae-4536-c05c-6b5aeba44e2a": "5a284f0bc7d30d13bc081a28"
		};
		let datasets = {};
		if(this.state.inputs["Buildings"]){
			datasets["39207485-50d8-4792-a395-bbda06ec5fc4"]= this.state.inputs["Buildings"];
		}
		if(this.state.inputs["Mean Damage"]){
			datasets["695f5982-add6-47c0-c07e-d88928467f63"]= this.state.inputs["Mean Damage"];
		}
		await this.props.executeAnalysis(workflowId, creatorId, this.state.title, this.state.description, parameters, datasets);

		await wait(5000);
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
