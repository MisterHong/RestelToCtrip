package com.supyuan.system.hotel;

public class HotelModel {
	private String pais;	//国家
	private String codigo_hotel;	//Hotel’s cobol code
	private String codigo;
	private String hot_afiliacion;	//Hotel affiliation
	private String nombre_h;	//Hotel name
	private String direccion;	//Hotel address
	private String codprovincia;	//酒店省的数字代码
	private String provincia;	//Hotel province
	private String codpoblacion;	//Numerical code of hotel city
	private String poblacion;	//Hotel city
	private String cp;	//酒店邮政编码
	private String mail;	//Hotel email 
	private String web;	//Hotel website
	private String telefono;	//Hotel telephone
	private String fotos;	//标签包含酒店的不同照片
	private String plano;	//Hotel map
	private String desc_hotel;	//酒店描述的语言表示
	private String num_habitaciones;	//酒店的总客房数量
	private String como_llegar;	//有关如何到达酒店的文字
	private String tipo_establecimiento;	//酒店建筑的分类 //1（酒店），2（公寓式酒店），3（汽车旅馆），4（公寓），5（旅馆），6（养老金），7（养老金住宅），8（度假村），9（别墅），10（宿舍），11（农村），12（riad），13（住宅）和14（露营）。
	private String categoria;	//酒店类别 Hotel category
	private String checkin;	//在酒店办理入住手续的时间
	private String checkout;	//退房时间在酒店
	private String edadnindes;	//客人被视为儿童开始的年龄
	private String edadninhas;	//客人被视为儿童的结束年龄
	private String currency;	//Hotel currency 酒店货币
	private String longitude; 	//酒店经度
	private String latitude; 	//酒店纬度
	
	public HotelModel(){}

	public HotelModel(String pais, String codigo_hotel, String hot_afiliacion, String nombre_h, String direccion,
			String codprovincia, String provincia, String codpoblacion, String poblacion, String cp, String mail,
			String web, String telefono, String fotos, String plano, String desc_hotel, String num_habitaciones,
			String como_llegar, String tipo_establecimiento, String categoria, String checkin, String checkout,
			String edadnindes, String edadninhas, String currency) {
		super();
		this.pais = pais;
		this.codigo_hotel = codigo_hotel;
		this.hot_afiliacion = hot_afiliacion;
		this.nombre_h = nombre_h;
		this.direccion = direccion;
		this.codprovincia = codprovincia;
		this.provincia = provincia;
		this.codpoblacion = codpoblacion;
		this.poblacion = poblacion;
		this.cp = cp;
		this.mail = mail;
		this.web = web;
		this.telefono = telefono;
		this.fotos = fotos;
		this.plano = plano;
		this.desc_hotel = desc_hotel;
		this.num_habitaciones = num_habitaciones;
		this.como_llegar = como_llegar;
		this.tipo_establecimiento = tipo_establecimiento;
		this.categoria = categoria;
		this.checkin = checkin;
		this.checkout = checkout;
		this.edadnindes = edadnindes;
		this.edadninhas = edadninhas;
		this.currency = currency;
	}
	
	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getPais() {
		return pais;
	}
	public void setPais(String pais) {
		this.pais = pais;
	}
	public String getCodigo_hotel() {
		return codigo_hotel;
	}
	public void setCodigo_hotel(String codigo_hotel) {
		this.codigo_hotel = codigo_hotel;
	}
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getHot_afiliacion() {
		return hot_afiliacion;
	}
	public void setHot_afiliacion(String hot_afiliacion) {
		this.hot_afiliacion = hot_afiliacion;
	}
	public String getNombre_h() {
		return nombre_h;
	}
	public void setNombre_h(String nombre_h) {
		this.nombre_h = nombre_h;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public String getCodprovincia() {
		return codprovincia;
	}
	public void setCodprovincia(String codprovincia) {
		this.codprovincia = codprovincia;
	}
	public String getProvincia() {
		return provincia;
	}
	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}
	public String getCodpoblacion() {
		return codpoblacion;
	}
	public void setCodpoblacion(String codpoblacion) {
		this.codpoblacion = codpoblacion;
	}
	public String getPoblacion() {
		return poblacion;
	}
	public void setPoblacion(String poblacion) {
		this.poblacion = poblacion;
	}
	public String getCp() {
		return cp;
	}
	public void setCp(String cp) {
		this.cp = cp;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public String getWeb() {
		return web;
	}
	public void setWeb(String web) {
		this.web = web;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getFotos() {
		return fotos;
	}
	public void setFotos(String fotos) {
		this.fotos = fotos;
	}
	public String getPlano() {
		return plano;
	}
	public void setPlano(String plano) {
		this.plano = plano;
	}
	public String getDesc_hotel() {
		return desc_hotel;
	}
	public void setDesc_hotel(String desc_hotel) {
		this.desc_hotel = desc_hotel;
	}
	public String getNum_habitaciones() {
		return num_habitaciones;
	}
	public void setNum_habitaciones(String num_habitaciones) {
		this.num_habitaciones = num_habitaciones;
	}
	public String getComo_llegar() {
		return como_llegar;
	}
	public void setComo_llegar(String como_llegar) {
		this.como_llegar = como_llegar;
	}
	public String getTipo_establecimiento() {
		return tipo_establecimiento;
	}
	public void setTipo_establecimiento(String tipo_establecimiento) {
		this.tipo_establecimiento = tipo_establecimiento;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
	public String getCheckin() {
		return checkin;
	}
	public void setCheckin(String checkin) {
		this.checkin = checkin;
	}
	public String getCheckout() {
		return checkout;
	}
	public void setCheckout(String checkout) {
		this.checkout = checkout;
	}
	public String getEdadnindes() {
		return edadnindes;
	}
	public void setEdadnindes(String edadnindes) {
		this.edadnindes = edadnindes;
	}
	public String getEdadninhas() {
		return edadninhas;
	}
	public void setEdadninhas(String edadninhas) {
		this.edadninhas = edadninhas;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
