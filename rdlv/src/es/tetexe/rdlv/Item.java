package es.tetexe.rdlv;

public class Item {
	private Integer image;
	private String title;
	private String descrip;
	private Integer imgNext;

	public Item() {
		super();
	}

	public Item(Integer image, String title, String descrip, Integer imgNext) {
		super();
		this.image = image;
		this.title = title;
		this.descrip = descrip;
		this.imgNext = imgNext;
	}

	public Integer getImage() {
		return image;
	}

	public void setImage(Integer image) {
		this.image = image;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescrip() {
		return descrip;
	}

	public void setUrl(String descrip) {
		this.descrip = descrip;
	}

	public Integer getImgNext() {
		return imgNext;
	}

	public void setImgNext(Integer imgNext) {
		this.imgNext = imgNext;
	}
}
