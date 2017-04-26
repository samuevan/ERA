package ec.app.util;
import java.util.Comparator;

public class Pair<T extends Comparable<T>,U extends Comparable<U>> implements Comparable<Pair<T,U>>{
		private T first;
		private U second;
		
		
		public Pair(T first, U second) {
			this.first = first;
			this.second = second;
		}
		
		public T getFirst() {
			return first;
		}
		
		public void setFirst(T first) {
			this.first = first;
		}
		
		public U getSecond() {
			return second;
		}
		
		public void setSecond(U second) {
			this.second = second;
		}

		/*@Override
		public int compare(Object o1, Object o2) {
			// TODO Auto-generated method stub
			
			Pair<T,U> p1 = (Pair<T,U>) o1;
			Pair<T,U> p2 = (Pair<T,U>) o2;
			
			return ((int)p1.getSecond()) - ((int)p2.getSecond()); //TODO int??? é o tipo de retorno do comparator, mas é estranho
		}
		*/
		/*@Override
		public int compareTo(Object o1) {
			// TODO Auto-generated method stub
			Pair<T,U> p1 = this;
			Pair<T,U> p2 = (Pair<T,U>) o1;
			
			if(((double)p1.getSecond()) - ((double)p2.getSecond()) < 0){
				return -1;
			}
			else
			{
				if(((double)p1.getSecond()) - ((double)p2.getSecond()) > 0){
					return 1;
				}
				else
					return 0;
			}
			
		}*/

		@Override
		public int compareTo(Pair<T, U> o) {
			// TODO Auto-generated method stub
			return this.getSecond().compareTo(o.getSecond());
		}
		
		
	}
