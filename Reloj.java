public class Reloj{
	int horas;
	int minutos;
	
	public Reloj(){
		this.horas = 0;
		this.minutos = 0;
	}
	
	public Reloj(int horas, int minutos){
		this.horas = horas;
		this.minutos = minutos;
	}
	
	public Reloj(Reloj r){
		this.horas = r.getHoras();
		this.minutos = r.getMinutos();
	}
	
	public String toString() { 
		return horas + ":" + minutos;
	}
	
	public int getHoras(){
		return horas;
	}
	
	public void setHoras(int horas){
		this.horas = horas;
	}
	
	public int getMinutos(){
		return minutos;
	}
	
	public void setMinutos(int minutos){
		this.minutos = minutos;
	}
	
	public int getTiempoMinutos(){
		return ((this.horas*60)+(this.minutos));
	}
	
	public void setTiempoMinutos(int minutos){
		this.horas=minutos/60;
		this.minutos=minutos%60;
	}
}