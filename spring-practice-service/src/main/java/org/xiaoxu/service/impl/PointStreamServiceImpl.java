package org.xiaoxu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xiaoxu.domain.PointStream;
import org.xiaoxu.mapper.PointStreamMapper;
import org.xiaoxu.service.PointStreamService;

@Service
public class PointStreamServiceImpl extends ServiceImpl<PointStreamMapper, PointStream> implements PointStreamService {
}
