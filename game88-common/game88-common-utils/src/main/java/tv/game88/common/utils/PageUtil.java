package tv.game88.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PageUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	public static <T> List<T> pageBySubList( List<T> list, int pagesize, int currentPage ) {
		int     totalcount = list.size();
		int     pagecount  = 0;
		List<T> subList    = new ArrayList<>();
		int     m          = totalcount % pagesize;
		if ( m > 0 ) {
			pagecount = totalcount / pagesize + 1;
		} else {
			pagecount = totalcount / pagesize;
		}
		if ( ( ( currentPage - 1 ) * pagesize ) >= totalcount ) {
			return subList;
		}
		int toIndex = pagesize * ( currentPage ) > totalcount ? totalcount - 1 : pagesize * ( currentPage );
		if ( m == 0 ) {
			subList = list.subList( ( currentPage - 1 ) * pagesize, toIndex );
		} else {
			if ( currentPage == pagecount ) {
				subList = list.subList( ( currentPage - 1 ) * pagesize, totalcount );
			} else {
				subList = list.subList( ( currentPage - 1 ) * pagesize, toIndex );
			}
		}
		return subList;
	}

	public static List<List<?>> pageList( List list, int pagesize ) {

		List<List<?>> resultList=new ArrayList<List<?>>();

		int totalcount = list.size();
		int pagecount  = 0;
		int m          = totalcount % pagesize;
		if ( m > 0 ) {
			pagecount = totalcount / pagesize + 1;
		} else {
			pagecount = totalcount / pagesize;
		}

		for ( int i = 1; i <= pagecount; i++ ) {
			if ( m == 0 ) {
				List<?> subList = list.subList( ( i - 1 ) * pagesize, pagesize * ( i ) );
				resultList.add( subList );
			} else {
				if ( i == pagecount ) {
					List<?> subList = list.subList( ( i - 1 ) * pagesize, totalcount );
					resultList.add( subList );
				} else {
					List<?> subList = list.subList( ( i - 1 ) * pagesize, pagesize * ( i ) );
					resultList.add( subList );
				}
			}
		}
		return resultList;
	}
}
