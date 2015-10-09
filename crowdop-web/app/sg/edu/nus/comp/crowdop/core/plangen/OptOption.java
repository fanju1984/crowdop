package sg.edu.nus.comp.crowdop.core.plangen;

public class OptOption {
	public OptOption(String globalOpt, String selectOpt, String joinOpt) {
		glbOpt = globalOpt;
		selOpt = selectOpt;
		joOpt = joinOpt;
	}
	public String glbOpt = "";
	public String selOpt = "";
	public String joOpt = "";
	
	public static OptOption parse(String string) {
		String tmps[] = string.split("\\;");
		OptOption opt = new OptOption (tmps[0], tmps[1], tmps[2]);
		return opt;
	}
	
	public String toString () {
		return glbOpt + "\t" + selOpt + "\t" + joOpt;
	}
	
	public static OptOption getDefaultOption () {
		OptOption optOption = OptOption.parse("CrowdOpOpt;CrowdOp;CrowdOp-App");
		return optOption;
	}
	
}
