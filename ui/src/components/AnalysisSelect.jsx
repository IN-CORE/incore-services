import React, {Component} from "react";
import {SelectField, MenuItem} from "material-ui";

class AnalysisSelect extends Component {

	constructor(props) {
		super(props);
		this.state = {};
		this.onChange = this.onChange.bind(this);
	}
	onChange(event: Object, index, value) {
		this.props.onSelectAnalysis(value);
		this.setState({selectedAnalysis: value});
	}

	render() {
		const options = this.props.analyses.map( d =>
			<MenuItem value={d.id} key={d.id} primaryText={d.name}/>
		);

		return (
			<div>
				<SelectField onChange={this.onChange} value={this.state.selectedAnalysis}>
					{options}
				</SelectField >
			</div>
		);
	}

}

export default AnalysisSelect;
