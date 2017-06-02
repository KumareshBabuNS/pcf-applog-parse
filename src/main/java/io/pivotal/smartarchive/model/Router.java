package io.pivotal.smartarchive.model;

public class Router {
	String id;
	String time;
	String traceid;
	String spanid;
	String sourceinstance;
	String messagetype;
	String sourcetype;
	String appid;
	String _1; //reqhost
	String _2; //reqstart;
	String _3; //reqmethodandurl;
	String _4; //statuscode;
	String _5; //bytesrecv;
	String _6; //bytessent;
	String _7; //referer;
	String _8; //uagent;
	String _9; //sourceaddress;
	String _10; //sourceport;
	String _11; //remoteaddress;
	String _12; //remoteport;
	String _13; //xforwardedfor;
	String _14; //xforwardprotocol;
	String _15; //vcapreq;
	String _16; //resptime;
	String _17; //app_id;
	String _18; //appindex;
	String _19; //x_b3_traceid;
	String _20; //x_b3_spanid;
	String _21; //x_b3_parentspanid;
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTraceid() {
		return traceid;
	}
	public void setTraceid(String traceid) {
		this.traceid = traceid;
	}
	public String getSpanid() {
		return spanid;
	}
	public void setSpanid(String spanid) {
		this.spanid = spanid;
	}
	public String getSourceinstance() {
		return sourceinstance;
	}
	public void setSourceinstance(String sourceinstance) {
		this.sourceinstance = sourceinstance;
	}
	public String getMessagetype() {
		return messagetype;
	}
	public void setMessagetype(String messagetype) {
		this.messagetype = messagetype;
	}
	public String getSourcetype() {
		return sourcetype;
	}
	public void setSourcetype(String sourcetype) {
		this.sourcetype = sourcetype;
	}
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Router other = (Router) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public String get_1() {
		return _1;
	}
	public void set_1(String _1) {
		this._1 = _1;
	}
	public String get_2() {
		return _2;
	}
	public void set_2(String _2) {
		this._2 = _2;
	}
	public String get_3() {
		return _3;
	}
	public void set_3(String _3) {
		this._3 = _3;
	}
	public String get_4() {
		return _4;
	}
	public void set_4(String _4) {
		this._4 = _4;
	}
	public String get_5() {
		return _5;
	}
	public void set_5(String _5) {
		this._5 = _5;
	}
	public String get_6() {
		return _6;
	}
	public void set_6(String _6) {
		this._6 = _6;
	}
	public String get_7() {
		return _7;
	}
	public void set_7(String _7) {
		this._7 = _7;
	}
	public String get_8() {
		return _8;
	}
	public void set_8(String _8) {
		this._8 = _8;
	}
	public String get_9() {
		return _9;
	}
	public void set_9(String _9) {
		this._9 = _9;
	}
	public String get_10() {
		return _10;
	}
	public void set_10(String _10) {
		this._10 = _10;
	}
	public String get_11() {
		return _11;
	}
	public void set_11(String _11) {
		this._11 = _11;
	}
	public String get_12() {
		return _12;
	}
	public void set_12(String _12) {
		this._12 = _12;
	}
	public String get_13() {
		return _13;
	}
	public void set_13(String _13) {
		this._13 = _13;
	}
	public String get_14() {
		return _14;
	}
	public void set_14(String _14) {
		this._14 = _14;
	}
	public String get_15() {
		return _15;
	}
	public void set_15(String _15) {
		this._15 = _15;
	}
	public String get_16() {
		return _16;
	}
	public void set_16(String _16) {
		this._16 = _16;
	}
	public String get_17() {
		return _17;
	}
	public void set_17(String _17) {
		this._17 = _17;
	}
	public String get_18() {
		return _18;
	}
	public void set_18(String _18) {
		this._18 = _18;
	}
	public String get_19() {
		return _19;
	}
	public void set_19(String _19) {
		this._19 = _19;
	}
	public String get_20() {
		return _20;
	}
	public void set_20(String _20) {
		this._20 = _20;
	}
	public String get_21() {
		return _21;
	}
	public void set_21(String _21) {
		this._21 = _21;
	}
	
}
